package com.vlox.hud;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

/**
 * Tracks which movement and mouse buttons are held, and how fast they are clicked.
 *
 * The state is sampled from the key bindings rather than hooked into the input
 * pipeline, so VLOX can show keystrokes and CPS without a mixin. Clicks are
 * recorded as a rising edge in a small ring buffer of timestamps, which means
 * nothing is allocated while sampling.
 */
public final class CombatInput {
	private static final int CAPACITY = 32;
	private static final long WINDOW_MS = 1000L;

	private static final long[] LEFT_CLICKS = new long[CAPACITY];
	private static final long[] RIGHT_CLICKS = new long[CAPACITY];
	private static int leftHead;
	private static int rightHead;

	private static boolean leftWasDown;
	private static boolean rightWasDown;

	public static boolean up;
	public static boolean down;
	public static boolean left;
	public static boolean right;
	public static boolean jump;
	public static boolean sneak;
	public static boolean sprint;
	public static boolean attack;
	public static boolean use;

	private CombatInput() {
	}

	/** Samples the inputs. Cheap enough to call once per frame. */
	public static void poll(Minecraft minecraft) {
		if (minecraft == null || minecraft.options == null) {
			return;
		}
		Options options = minecraft.options;

		up = isDown(options.keyUp);
		down = isDown(options.keyDown);
		left = isDown(options.keyLeft);
		right = isDown(options.keyRight);
		jump = isDown(options.keyJump);
		sneak = isDown(options.keyShift);
		sprint = isDown(options.keySprint);
		attack = isDown(options.keyAttack);
		use = isDown(options.keyUse);

		long now = System.currentTimeMillis();
		if (attack && !leftWasDown) {
			push(LEFT_CLICKS, leftHead, now);
			leftHead = (leftHead + 1) % CAPACITY;
		}
		if (use && !rightWasDown) {
			push(RIGHT_CLICKS, rightHead, now);
			rightHead = (rightHead + 1) % CAPACITY;
		}
		leftWasDown = attack;
		rightWasDown = use;
	}

	/** Clicks in the last second on the given button. */
	public static int cps(boolean rightButton) {
		long since = System.currentTimeMillis() - WINDOW_MS;
		long[] ring = rightButton ? RIGHT_CLICKS : LEFT_CLICKS;
		int count = 0;
		for (long stamp : ring) {
			if (stamp > since) {
				count++;
			}
		}
		return count;
	}

	private static boolean isDown(KeyMapping mapping) {
		return mapping != null && mapping.isDown();
	}

	private static void push(long[] ring, int index, long value) {
		ring[index] = value;
	}
}
