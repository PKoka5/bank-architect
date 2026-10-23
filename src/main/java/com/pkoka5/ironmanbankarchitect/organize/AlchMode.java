package com.pkoka5.ironmanbankarchitect.organize;

public enum AlchMode
{
	FLEXIBLE("Flexible"), FIXED("Fixed list");

	private final String label;
	AlchMode(String label) { this.label = label; }
	@Override public String toString() { return label; }
}
