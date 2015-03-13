package com.Jerry.GPSArea;

import java.util.ArrayList;

import com.Jerry.GPSApp.R;
import com.Jerry.GlobalFun.GlobalFun;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

public class AreaView extends View {
	private Paint				paint;
	private static int			viewWidth, viewHeight;
	/** 纬度或者经度变化 1 所代表的位移 */
	private static final double	unitWidth		= 111319.49079327357;
	/** 画点的半径 */
	private static final float	radius			= 4;
	/** 画线的宽度 */
	private static final float	strokeWidth		= 3;
	/** 背景框空白的宽度和高度 */
	private static final int	spaceWidth		= 40;
	private static final int	spaceHeight		= 40;
	/** 指南针图片资源 */
	private Bitmap				compassBitmap	= null;
	/** 默认的坐标转换的倍数 */
	private static float		defaultScale	= 80000;
	/** 坐标转换的倍数 */
	private float				scale;
	/** view 原点坐标 */
	private static Point		zeroPoint		= new Point();
	/** 起点的 地理坐标 */
	private Point				startPoint		= new Point();
	/** 终点的 地理坐标 */
	private Point				stopPoint		= new Point();
	private boolean				isEnd			= false;
	/** 所有坐标点的Vector */
	private ArrayList<Point>	coordinateList	= new ArrayList<Point>();

	public AreaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		compassBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.compass)).getBitmap();
		paint = new Paint();
		reset();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 得到view的高度和宽度
		viewWidth = getWidth();
		viewHeight = getHeight();
		zeroPoint = new Point(viewWidth / 2, viewHeight / 2);
		// 设置黑色背景
		canvas.drawColor(Color.BLACK);
		// 取消锯齿
		paint.setAntiAlias(true);

		// 先画背景框
		Paint bgPaint = new Paint();
		bgPaint.setAntiAlias(true);
		bgPaint.setColor(Color.GRAY);
		drawBackground(canvas, bgPaint, spaceWidth, spaceHeight);
		// 画指南针
		Paint compassPaint = new Paint();
		compassPaint.setAntiAlias(true);
		compassPaint.setColor(Color.GREEN);
		compassPaint.setTextSize(16);
		drawCompass(canvas, compassPaint);
		// 画比例尺
		Paint scalePaint = new Paint();
		scalePaint.setAntiAlias(true);
		scalePaint.setColor(Color.RED);
		drawScale(canvas, scalePaint);

		// 先画起点 绿色
		paint.setColor(Color.GREEN);
		drawPoint(canvas, paint, zeroPoint, radius);
		// 画中间各点
		paint.setColor(Color.BLUE);
		drawRect(canvas, paint, coordinateList);
		// 如果结束,画终点和起点的连线
		if (isEnd) {
			drawLine(canvas, paint, strokeWidth, changeCoordinate(startPoint, stopPoint, scale),
				zeroPoint);
		}
	}

	/** 画出背景 框 */
	private void drawBackground(Canvas canvas, Paint paint, int width, int height) {
		float lineWidth = 1;
		paint.setStrokeWidth(lineWidth);
		int numW = viewWidth / width;
		int numH = viewHeight / height;
		/** 画竖线 */
		for(int i = 0; i <= numW; i++) {
			drawLine(canvas, paint, lineWidth, new Point(width * i, 0), new Point(width * i, viewHeight));
		}
		/** 画横线 */
		for(int i = 0; i <= numH; i++) {
			drawLine(canvas, paint, lineWidth, new Point(0, height * i),
				new Point(viewWidth, height * i));
		}
	}

	/** 画南北标识 */
	private void drawCompass(Canvas canvas, Paint paint) {
		canvas.drawBitmap(compassBitmap, 10, 10, null);
		canvas.drawText("N", 25, 15, paint);
		canvas.drawText("S", 25, 70, paint);
	}

	/** 画比例尺 */
	private void drawScale(Canvas canvas, Paint paint) {
		canvas.drawLine(10, viewHeight - 15, 10, viewHeight - 10, paint);
		canvas.drawLine(10, viewHeight - 10, 10 + spaceWidth, viewHeight - 10, paint);
		canvas.drawLine(10 + spaceWidth, viewHeight - 10, 10 + spaceWidth, viewHeight - 15, paint);
		canvas.drawText((int) (unitWidth / scale) * spaceWidth + " m", 10, viewHeight - 20, paint);
	}

	/** 画点(view 坐标) */
	private void drawPoint(Canvas canvas, Paint paint, Point point, float radius) {
		canvas.drawCircle((float) point.x, (float) point.y, radius, paint);
	}

	/** 画线(view 坐标) */
	private void drawLine(Canvas canvas, Paint paint, float width, Point startPoint, Point stopPoint) {
		paint.setStrokeWidth(width);
		canvas.drawLine((float) startPoint.x, (float) startPoint.y, (float) stopPoint.x,
			(float) stopPoint.y, paint);
	}

	/**
	 * 画出区域,点线连接
	 * 
	 * @param pointVector 坐标的容器
	 */
	private void drawRect(Canvas canvas, Paint paint, ArrayList<Point> pointVector) {
		Point start = new Point();
		Point stop = new Point();
		int len = pointVector.size();
		if (len > 1) {
			for(int i = 1; i < len; i++) {
				if (1 == i) {
					start = zeroPoint;
				} else {
					start = changeCoordinate(startPoint, coordinateList.get(i - 1), scale);
				}
				stop = changeCoordinate(startPoint, coordinateList.get(i), scale);
				drawPoint(canvas, paint, stop, radius);
				drawLine(canvas, paint, strokeWidth, start, stop);
				// 超出边界 处理
				if (stop.y <= 0 || stop.y >= viewHeight) {
					scale = (float) Math.abs(scale * 0.8 * (viewHeight / 2) / (stop.y - viewHeight / 2));
					postInvalidate();
				} else if (stop.x <= 0 || stop.x >= viewWidth) {
					scale = (float) Math.abs(scale * 0.8 * (viewWidth / 2) / (stop.x - viewWidth / 2));
					postInvalidate();
				}
			}
		}
	}

	/** 设置起点坐标(地理 坐标) */
	public void setStartPoint(double x, double y) {
		startPoint.x = x;
		startPoint.y = y;
		addPoint(startPoint);
		isEnd = false;
	}
	/** 设置终点坐标(地理 坐标) */
	public void setStopPoint(double x, double y) {
		stopPoint.x = x;
		stopPoint.y = y;
		addPoint(stopPoint);
		isEnd = true;
	}

	/** 添加坐标点(x,y) */
	public void addPoint(double x, double y) {
		coordinateList.add(new Point(x, y));
	}
	/** 添加坐标点(point) */
	public void addPoint(Point point) {
		coordinateList.add(point);
	}
	/** areaView 数据 初始化 */
	public void reset() {
		coordinateList.clear();
		scale = defaultScale;
		isEnd = false;
		postInvalidate();
	}
	/** 返回坐标容器 */
	public ArrayList<Point> getPoints() {
		return coordinateList;
	}
	/** 计算总面积 */
	public double getArea() {
		double result = 0.0;
		result = GlobalFun.calcArea(coordinateList);
		return result;
	}

	/**
	 * 坐标转换,将地理坐标转换为view 坐标
	 * 
	 * @param startPoint 起点的 地理坐标
	 * @param point 需要转换的 地理坐标
	 * @param scale 转换倍数
	 * @return 返回转换后的坐标
	 */
	public Point changeCoordinate(Point startPoint, Point point, float scale) {
		Point result = new Point();
		result.x = (point.x - startPoint.x) * scale + zeroPoint.x;
		result.y = (point.y - startPoint.y) * scale + zeroPoint.y;
		return result;
	}
}
