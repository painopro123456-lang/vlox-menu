package com.vlox.feature;

import java.util.function.Consumer;

/**
 * A single setting in the VLOX menu.
 *
 * A feature knows how to describe itself, what value range it accepts, and
 * optionally what to do when its value changes (for features that forward to a
 * vanilla option). The value itself lives in {@link com.vlox.config.VloxConfig}.
 */
public final class Feature {
	public enum Type {
		TOGGLE,
		NUMBER,
		CHOICE,
		ACTION
	}

	public final String id;
	public final String label;
	public final String description;
	public final Category category;
	public final Type type;

	public final double min;
	public final double max;
	public final double step;
	public final double def;

	public final String[] choices;

	private Consumer<Feature> listener;

	private Feature(String id, String label, String description, Category category, Type type,
			double min, double max, double step, double def, String[] choices) {
		this.id = id;
		this.label = label;
		this.description = description;
		this.category = category;
		this.type = type;
		this.min = min;
		this.max = max;
		this.step = step;
		this.def = def;
		this.choices = choices;
	}

	public static Feature toggle(Category category, String id, String label, String description, boolean def) {
		return new Feature(id, label, description, category, Type.TOGGLE, 0, 1, 1, def ? 1 : 0, null);
	}

	public static Feature number(Category category, String id, String label, String description,
			double min, double max, double step, double def) {
		return new Feature(id, label, description, category, Type.NUMBER, min, max, step, def, null);
	}

	public static Feature choice(Category category, String id, String label, String description,
			String[] choices, int defIndex) {
		return new Feature(id, label, description, category, Type.CHOICE, 0, choices.length - 1, 1, defIndex, choices);
	}

	public static Feature action(Category category, String id, String label, String description) {
		return new Feature(id, label, description, category, Type.ACTION, 0, 0, 1, 0, null);
	}

	/** Registers the behaviour to run after this feature's value changes. */
	public Feature onChange(Consumer<Feature> listener) {
		this.listener = listener;
		return this;
	}

	public void fireChange() {
		if (this.listener != null) {
			this.listener.accept(this);
		}
	}

	public String describeValue(double value) {
		if (this.type == Type.TOGGLE) {
			return value != 0 ? "On" : "Off";
		}
		if (this.type == Type.CHOICE) {
			int index = (int) Math.round(value);
			if (index < 0 || index >= this.choices.length) {
				index = 0;
			}
			return this.choices[index];
		}
		if (this.step >= 1) {
			return Long.toString(Math.round(value));
		}
		return String.format("%.2f", value);
	}
}
