package com.Jerry.GPSArea;

/** 自定义的Point坐标类 */
public class Point {
	public double	x;
	public double	y;

	public Point() {
		this.x = 0.0;
		this.y = 0.0;
	}
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public Point(int x, int y) {
		this.x = (double) x;
		this.y = (double) y;
	}
	public Point(float x, float y) {
		this.x = (double) x;
		this.y = (double) y;
	}
}
