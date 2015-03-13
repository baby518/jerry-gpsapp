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
	/** γ�Ȼ��߾��ȱ仯 1 �������λ�� */
	private static final double	unitWidth		= 111319.49079327357;
	/** ����İ뾶 */
	private static final float	radius			= 4;
	/** ���ߵĿ�� */
	private static final float	strokeWidth		= 3;
	/** ������հ׵Ŀ�Ⱥ͸߶� */
	private static final int	spaceWidth		= 40;
	private static final int	spaceHeight		= 40;
	/** ָ����ͼƬ��Դ */
	private Bitmap				compassBitmap	= null;
	/** Ĭ�ϵ�����ת���ı��� */
	private static float		defaultScale	= 80000;
	/** ����ת���ı��� */
	private float				scale;
	/** view ԭ������ */
	private static Point		zeroPoint		= new Point();
	/** ���� �������� */
	private Point				startPoint		= new Point();
	/** �յ�� �������� */
	private Point				stopPoint		= new Point();
	private boolean				isEnd			= false;
	/** ����������Vector */
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
		// �õ�view�ĸ߶ȺͿ��
		viewWidth = getWidth();
		viewHeight = getHeight();
		zeroPoint = new Point(viewWidth / 2, viewHeight / 2);
		// ���ú�ɫ����
		canvas.drawColor(Color.BLACK);
		// ȡ�����
		paint.setAntiAlias(true);

		// �Ȼ�������
		Paint bgPaint = new Paint();
		bgPaint.setAntiAlias(true);
		bgPaint.setColor(Color.GRAY);
		drawBackground(canvas, bgPaint, spaceWidth, spaceHeight);
		// ��ָ����
		Paint compassPaint = new Paint();
		compassPaint.setAntiAlias(true);
		compassPaint.setColor(Color.GREEN);
		compassPaint.setTextSize(16);
		drawCompass(canvas, compassPaint);
		// ��������
		Paint scalePaint = new Paint();
		scalePaint.setAntiAlias(true);
		scalePaint.setColor(Color.RED);
		drawScale(canvas, scalePaint);

		// �Ȼ���� ��ɫ
		paint.setColor(Color.GREEN);
		drawPoint(canvas, paint, zeroPoint, radius);
		// ���м����
		paint.setColor(Color.BLUE);
		drawRect(canvas, paint, coordinateList);
		// �������,���յ����������
		if (isEnd) {
			drawLine(canvas, paint, strokeWidth, changeCoordinate(startPoint, stopPoint, scale),
				zeroPoint);
		}
	}

	/** �������� �� */
	private void drawBackground(Canvas canvas, Paint paint, int width, int height) {
		float lineWidth = 1;
		paint.setStrokeWidth(lineWidth);
		int numW = viewWidth / width;
		int numH = viewHeight / height;
		/** ������ */
		for(int i = 0; i <= numW; i++) {
			drawLine(canvas, paint, lineWidth, new Point(width * i, 0), new Point(width * i, viewHeight));
		}
		/** ������ */
		for(int i = 0; i <= numH; i++) {
			drawLine(canvas, paint, lineWidth, new Point(0, height * i),
				new Point(viewWidth, height * i));
		}
	}

	/** ���ϱ���ʶ */
	private void drawCompass(Canvas canvas, Paint paint) {
		canvas.drawBitmap(compassBitmap, 10, 10, null);
		canvas.drawText("N", 25, 15, paint);
		canvas.drawText("S", 25, 70, paint);
	}

	/** �������� */
	private void drawScale(Canvas canvas, Paint paint) {
		canvas.drawLine(10, viewHeight - 15, 10, viewHeight - 10, paint);
		canvas.drawLine(10, viewHeight - 10, 10 + spaceWidth, viewHeight - 10, paint);
		canvas.drawLine(10 + spaceWidth, viewHeight - 10, 10 + spaceWidth, viewHeight - 15, paint);
		canvas.drawText((int) (unitWidth / scale) * spaceWidth + " m", 10, viewHeight - 20, paint);
	}

	/** ����(view ����) */
	private void drawPoint(Canvas canvas, Paint paint, Point point, float radius) {
		canvas.drawCircle((float) point.x, (float) point.y, radius, paint);
	}

	/** ����(view ����) */
	private void drawLine(Canvas canvas, Paint paint, float width, Point startPoint, Point stopPoint) {
		paint.setStrokeWidth(width);
		canvas.drawLine((float) startPoint.x, (float) startPoint.y, (float) stopPoint.x,
			(float) stopPoint.y, paint);
	}

	/**
	 * ��������,��������
	 * 
	 * @param pointVector ���������
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
				// �����߽� ����
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

	/** �����������(���� ����) */
	public void setStartPoint(double x, double y) {
		startPoint.x = x;
		startPoint.y = y;
		addPoint(startPoint);
		isEnd = false;
	}
	/** �����յ�����(���� ����) */
	public void setStopPoint(double x, double y) {
		stopPoint.x = x;
		stopPoint.y = y;
		addPoint(stopPoint);
		isEnd = true;
	}

	/** ��������(x,y) */
	public void addPoint(double x, double y) {
		coordinateList.add(new Point(x, y));
	}
	/** ��������(point) */
	public void addPoint(Point point) {
		coordinateList.add(point);
	}
	/** areaView ���� ��ʼ�� */
	public void reset() {
		coordinateList.clear();
		scale = defaultScale;
		isEnd = false;
		postInvalidate();
	}
	/** ������������ */
	public ArrayList<Point> getPoints() {
		return coordinateList;
	}
	/** ��������� */
	public double getArea() {
		double result = 0.0;
		result = GlobalFun.calcArea(coordinateList);
		return result;
	}

	/**
	 * ����ת��,����������ת��Ϊview ����
	 * 
	 * @param startPoint ���� ��������
	 * @param point ��Ҫת���� ��������
	 * @param scale ת������
	 * @return ����ת���������
	 */
	public Point changeCoordinate(Point startPoint, Point point, float scale) {
		Point result = new Point();
		result.x = (point.x - startPoint.x) * scale + zeroPoint.x;
		result.y = (point.y - startPoint.y) * scale + zeroPoint.y;
		return result;
	}
}
