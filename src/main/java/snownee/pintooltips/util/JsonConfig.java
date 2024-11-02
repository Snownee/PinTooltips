package snownee.pintooltips.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;
import snownee.pintooltips.PinTooltips;

public class JsonConfig<T> {

	public static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.serializeNulls()
			.enableComplexMapKeySerialization()
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.setLenient()
			.create();

	private final Path path;
	private final Codec<T> codec;
	private final CachedSupplier<T> configGetter;

	public JsonConfig(Path path, Codec<T> codec, @Nullable Runnable onUpdate, Supplier<T> defaultFactory) {
		this.path = path;
		this.codec = codec;
		this.configGetter = new CachedSupplier<>(() -> {
			if (Files.notExists(path)) {
				T def = defaultFactory.get();
				write(def, false);
				return def;
			}
			try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				return codec.parse(JsonOps.INSTANCE, GSON.fromJson(reader, JsonElement.class))
						.get()
						.left()
						.orElseThrow();
			} catch (Throwable e) {
				PinTooltips.LOGGER.error("Failed to read config file {}", this.path, e);
				if (this.path.getNameCount() > 0) {
					try {
						Files.move(this.path, this.path.resolveSibling(this.path.getFileName() + ".invalid"));
					} catch (Exception ignored) {
					}
				}
				T def = defaultFactory.get();
				write(def, false);
				return def;
			}
		});
		configGetter.onUpdate = onUpdate;
	}

	public JsonConfig(Path path, Codec<T> codec, @Nullable Runnable onUpdate) {
		this(
				path,
				codec,
				onUpdate,
				() -> codec.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap())
						.result()
						.orElseThrow()
		);
	}

	public T get() {
		return configGetter.get();
	}

	public void save() {
		write(get(), false); // Does not need to invalidate since the saved instance already has updated values
	}

	public void write(T t, boolean invalidate) {
		try {
			Files.createDirectories(path.getParent());
		} catch (IOException ignored) {
		}

		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			writer.write(GSON.toJson(codec.encodeStart(JsonOps.INSTANCE, t).get().left().orElseThrow()));
			if (invalidate) {
				invalidate();
			}
		} catch (Throwable e) {
			PinTooltips.LOGGER.error("Failed to write config file %s".formatted(path), e);
		}
	}

	public void invalidate() {
		configGetter.invalidate();
	}

	public Path getPath() {
		return path;
	}

	static class CachedSupplier<T> {

		private final Supplier<T> supplier;
		private T value;
		private Runnable onUpdate;

		public CachedSupplier(Supplier<T> supplier) {
			this.supplier = supplier;
		}

		public T get() {
			if (value == null) {
				synchronized (this) {
					value = supplier.get();
					Objects.requireNonNull(value);
					if (onUpdate != null) {
						onUpdate.run();
					}
				}
			}
			return value;
		}

		public void invalidate() {
			this.value = null;
		}
	}
}