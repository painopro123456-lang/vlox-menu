package com.vlox.feature;

public enum Category {
	HUD("HUD"),
	GRAPHICS("Graphics"),
	INPUT("Input"),
	CHAT("Chat"),
	MENU("Menu");

	public final String label;

	Category(String label) {
		this.label = label;
	}
}
