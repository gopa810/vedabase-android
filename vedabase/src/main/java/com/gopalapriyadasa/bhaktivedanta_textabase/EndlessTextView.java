package com.gopalapriyadasa.bhaktivedanta_textabase;

import java.util.ArrayList;

import com.gopalapriyadasa.textabase_engine.FDCharFormat;
import com.gopalapriyadasa.textabase_engine.FDDrawRecordContext;
import com.gopalapriyadasa.textabase_engine.FDDrawTextContext;
import com.gopalapriyadasa.textabase_engine.FDLink;
import com.gopalapriyadasa.textabase_engine.FDParagraph;
import com.gopalapriyadasa.textabase_engine.FDPartBase;
import com.gopalapriyadasa.textabase_engine.FDPartSized;
import com.gopalapriyadasa.textabase_engine.FDRecordBase;
import com.gopalapriyadasa.textabase_engine.FDRecordLocation;
import com.gopalapriyadasa.textabase_engine.FDRecordPart;
import com.gopalapriyadasa.textabase_engine.FDSelection;
import com.gopalapriyadasa.textabase_engine.FDTypeface;
import com.gopalapriyadasa.textabase_engine.FlatFileDestination;
import com.gopalapriyadasa.textabase_engine.FlatFileSourceInterface;
import com.gopalapriyadasa.textabase_engine.FlatParagraph;
import com.gopalapriyadasa.textabase_engine.VBCustomHighlights;
import com.gopalapriyadasa.textabase_engine.VBHighlighterAnchor;
import com.gopalapriyadasa.textabase_engine.VBCustomNotes;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class EndlessTextView extends View implements 
			GestureDetector.OnGestureListener, 
			GestureDetector.OnDoubleTapListener,
			ScaleGestureDetector.OnScaleGestureListener,
			FlatFileDestination, Callback {


	private boolean selectionOrderNormal = true;
	private boolean selectionOrderNormalOrig = true;

	public class TextPosition {
		public int record = 0;
		public float offset = 0;
	}

	public final static int TRACK_NONE = 0;
	public final static int TRACK_WAIT_NEXT = 1;
	public final static int TRACK_DRAG_CONTENT = 2;
	public final static int TRACK_DRAG_SELECTION = 3;
	public final static int TRACK_MULTITOUCH = 4;
	
	public boolean disableTouchDrag = false; 
	public Paint currentPaint = null;
	private PointF lastMultitouchCenterPoint = new PointF();
	private PointF multitouchCenterPoint = new PointF();
	private float startMultiplyFontSize = 1.5f;
	private int trackingMode = TRACK_NONE;
	private static String DEBUG_TAG = "ClickEvent";
	private ActionMode actionModeStarted = null;

	private float selectionHotSpotOffsetY = 116;
	private float selectionHotSpotOffsetX = 50;
	
	// text position history
	private TextPosition currentPos;
	private ArrayList<TextPosition> history;
	private int historyPos;
    private float yCurrTrace = 0;
	
	//public GestureDetector mDetector; 
	//public ScaleGestureDetector mScaleDetector;
	public EndlessTextViewCallback delegate = null;
	public boolean drawLineBeforeRecord = false;
	public boolean drawRecordNumber = false;
	
	//public int touchMode = 0;
	public float startX = 0;
	public float startY = 0;
	public float lastX = 0;
	public float lastY = 0;
	public float startDist = 0;
	public float lastDist = 0;
	public float startRatio = 0;
	public float lastRatio = 0;
	public FlatFileSourceInterface source = null;
	
	public float moveSensitivityX = 2.0f;
	public float moveSensitivityY = 2.0f;
	public long longClickTimeout = 750;
	
	public float paddingLeft = 64;
	public float paddingRight = 64;
	
	public FDRecordLocation[] selectionPoints = new FDRecordLocation[2];
	public FDRecordLocation[] orderedPoints = new FDRecordLocation[2];
	public int currentSelectionPoint = -1;
	public float currentHotSpotCorrection = 0;
	public float selectionHotSpotMaxDistance = 48;
	private Bitmap noteBitmap = null;
    private Bitmap noteBitmapX = null;
	private Bitmap bmpSelMarkA = null;
	private Bitmap bmpSelMarkB = null;
    private Bitmap visitedBitmap = null;
	
	private ArrayList<FDRecordBase> paintedRecords = new ArrayList<FDRecordBase>();
	
	public Paint notePaint = new Paint();
	private float lastDifferenceY = 0;
	private boolean fadeScrolling = false;

	public Bitmap leftEdgeBitmap = null;
	public Bitmap rightEdgeBitmap = null;
    public float edgeWidth = 0;
    public float edgeHeight = 0;
	
	public EndlessTextView(Context context) {
		super(context);
		notePaint.setTextSize(15);
		notePaint.setColor(Color.RED);
		initThisView(context);
		
		this.currentPos = new TextPosition();
		this.history = new ArrayList<EndlessTextView.TextPosition>();
		this.history.add(this.currentPos);
		this.historyPos = 0;
	}

	public void initThisView(Context context) {
		//mDetector = new GestureDetector(context, this);
		//mDetector.setOnDoubleTapListener(this);
		//mDetector.setIsLongpressEnabled(true);
		
		//mScaleDetector = new ScaleGestureDetector(context, this);


		if (!isInEditMode()) {
			selectionPoints[0] = null;
			selectionPoints[1] = null;

			noteBitmap = BitmapFactory.decodeResource(MainActivity.getInstance().getResources(), R.drawable.note_icon);
            noteBitmapX = BitmapFactory.decodeResource(MainActivity.getInstance().getResources(), R.drawable.note_icon_x);
			visitedBitmap = BitmapFactory.decodeResource(MainActivity.getInstance().getResources(), R.drawable.ok_orange);
			bmpSelMarkA = BitmapFactory.decodeResource(MainActivity.getInstance().getResources(), R.drawable.selection_mark_a);
			bmpSelMarkB = BitmapFactory.decodeResource(MainActivity.getInstance().getResources(), R.drawable.selection_mark_b);
			selectionHotSpotOffsetX = bmpSelMarkA.getWidth() / 2;
			selectionHotSpotOffsetY = bmpSelMarkA.getHeight() - selectionHotSpotOffsetX;
		}
	}

	public EndlessTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initThisView(context);
	}

	public EndlessTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		initThisView(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		float currX, currY;
		int tc = 0;
		long relativeEventTime = event.getEventTime() - event.getDownTime();
		currX = event.getX();
		currY = event.getY();
		switch(action) {
			case (MotionEvent.ACTION_DOWN) :
				fadeScrolling = false;
				lastDifferenceY = 0;
				if (trackingMode == TRACK_NONE)
				{
					startX = event.getX();
					startY = event.getY();
					trackingMode = onDownTouch(startX, startY);
				}
				selectionOrderNormalOrig = selectionOrderNormal;
				Log.i(DEBUG_TAG, "Action was DOWN, time is " + relativeEventTime);
				break;
			case (MotionEvent.ACTION_MOVE):
				if (trackingMode == TRACK_WAIT_NEXT) {
					if (movementIndicated(currX, currY))
					{
						// finger is moving
						//Log.i(DEBUG_TAG, "ScrollY, time is " + event.getDownTime());
						onDragContent(currX, currY);
						trackingMode = TRACK_DRAG_CONTENT;
					} else {
						// finger is in one place
						// this can end in click, clickLong
						//Log.i(DEBUG_TAG, "MoveEvent, time is " + (event.getEventTime() - event.getDownTime()));
						
						if (relativeEventTime > longClickTimeout) {
							trackingMode = onLongClickStart(currX, currY);
						}
					}
				} else if (trackingMode == TRACK_DRAG_CONTENT) {
					if (movementIndicated(currX, currY))
					{
						onDragContent(currX, currY);
					}
				} else if (trackingMode == TRACK_DRAG_SELECTION) { 
					if (movementIndicated(currX, currY))
					{
						onDragSelection(currX, currY);
					}
				} else if (trackingMode == TRACK_MULTITOUCH) {
					onScaleContent(event);
				}
				break;
			case (MotionEvent.ACTION_UP):
				if (trackingMode == TRACK_WAIT_NEXT) {
					onClick(currX, currY);
				} else if (trackingMode == TRACK_DRAG_CONTENT) {
					onDragContentEnd(currX, currY);
				} else if (trackingMode == TRACK_DRAG_SELECTION) {
					onDragSelectionEnd(currX, currY);
				}
				Log.i(DEBUG_TAG, "ActionUp, time is " + (event.getEventTime() - event.getDownTime()));
				trackingMode = TRACK_NONE;
				break;
			case (MotionEvent.ACTION_CANCEL):
				trackingMode = TRACK_NONE;
				break;
			/*case (MotionEvent.ACTION_SCROLL):
				Log.i(DEBUG_TAG, "Action was SCROLL");
				break;
			case (MotionEvent.ACTION_OUTSIDE):
				Log.i(DEBUG_TAG, "Action was OUTSIDE");
				break;*/
			case MotionEvent.ACTION_POINTER_DOWN:
				tc = event.getPointerCount(); 
				startDist = pointDistance(event);
				lastDist = startDist;
				startRatio = 1;
				lastRatio = 1;
				trackingMode = TRACK_MULTITOUCH;
				if (tc == 2) {
					onScaleContentStart(event);
				}
				break;
			case MotionEvent.ACTION_POINTER_UP:
				tc = event.getPointerCount();
				if (tc < 2) {
					trackingMode = TRACK_DRAG_CONTENT;
					onScaleContentEnd();
				}
				break;
			default:
				Log.i(DEBUG_TAG, "Action was " + action);
				break;
		}

		lastX = currX;
		lastY = currY;
		
		/*StringBuilder sb = new StringBuilder();
		
		pc = event.getPointerCount();
		sb.append("Pointers: count=" + pc);
		sb.append(", ");
		for(int i = 0; i < pc; i++) {
			sb.append("pt.id=" + event.getPointerId(i) + ", ");
		}
		
		Log.i(DEBUG_TAG, sb.toString());*/
		
		/*boolean b = mDetector.onTouchEvent(event);
		Log.i(DEBUG_TAG, "detector returned " + b);
		
		b = mScaleDetector.onTouchEvent(event);
		Log.i(DEBUG_TAG, "scale detector returned " + b);*/
		
		
		return true;
	}

	private void onScaleContentEnd() {
		
	}

	private void onScaleContentStart(MotionEvent event) {
		startDist = pointDistance(event);
		startMultiplyFontSize = FDCharFormat.getMultiplyFontSize();
		lastMultitouchCenterPoint.x = multitouchCenterPoint.x;
		lastMultitouchCenterPoint.y = multitouchCenterPoint.y;
	}

	public void onScaleContent(MotionEvent event) {
		float ratio = 1;
		float currDist = pointDistance(event);
		if (startDist > 10) {
			ratio = (float) Math.sqrt(currDist/startDist);
			float currentChange = Math.abs(ratio - lastRatio); 
			if (currentChange >= 0.03 && currentChange < 0.2)
			{
				float newM = startMultiplyFontSize * ratio;
				if (newM >= 1 && newM < 5) {
					FlatParagraph.setFontMultiplyer(newM);
					invalidate();
					Log.i(DEBUG_TAG, "Coordinates: " + ratio);
					lastRatio = ratio;
				}
				
			}
			float offsetChange = multitouchCenterPoint.y - lastMultitouchCenterPoint.y;
			if (offsetChange > 2) {
				Log.i(DEBUG_TAG, "Move multitouch: " + offsetChange);
				this.currentPos.offset += offsetChange;
                yCurrTrace -= offsetChange;
				lastMultitouchCenterPoint.y = multitouchCenterPoint.y;
				lastMultitouchCenterPoint.x = multitouchCenterPoint.x;
			}
		}
		else {
			startDist = currDist;
		}
	}

	private float pointDistance(MotionEvent event) {

		float r = 0;
		int pc = event.getPointerCount();
		if (pc == 2)
		{
			double x1, x2, y1, y2;
			x1 = event.getX(0);
			x2 = event.getX(1);
			y1 = event.getY(0);
			y2 = event.getY(1);
			multitouchCenterPoint.x = (float)((x1 + x2) / 2);
			multitouchCenterPoint.y = (float)((y1 + y2) / 2);
			r = (float)Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		}

		return r;
	}
	
	private void onDragSelectionEnd(float currX, float currY) {

		currentSelectionPoint = -1;
		trackingMode = TRACK_NONE;
		invalidate();
	}

	private void onDragContentEnd(float currX, float currY) {

		Log.i("lastFade", "lastDiff = " + lastDifferenceY);
		fadeScrolling = true;
		invalidate();
	}

	private void onClick(float currX, float currY) {

		FDRecordLocation hr;
		
		EndSelectionContext();
		
		hr = getHitLocation(currX, currY);

        if (hr.record != null && hr.record.isFullParaLink) {
            if (delegate != null) {
                delegate.endlessTextViewRecordClicked(hr.record);
            }
        } else if (hr.areaType == FDRecordLocation.AREA_PARA) {
			if (hr.cell != null) {
				FDPartBase cell = hr.cell;
				Log.i("ClickEvent", "hit item: " + hr.cell.orderNo);
				if (cell instanceof FDPartSized) {
					FDPartSized part = (FDPartSized)hr.cell;
					if (part.link != null) {
						Log.i("link", "clicked link 382738273");
						onNavigateLink(part.link);
					}
				}
			}
		} else if (hr.areaType == FDRecordLocation.AREA_LEFT_SIDE) {
			Log.i("track", "area left side");
			if (delegate != null) {
				delegate.endlessTextViewLeftAreaClicked(hr.record.recordId);
			}
		}
	}

	private void StartSelectionContext() {
		
		if (actionModeStarted == null)
			startActionMode(this);
	}
	
	private void EndSelectionContext() {
		processSelectionPoints(true);
		selectionPoints[0] = null;
		selectionPoints[1] = null;
		orderedPoints[0] = null;
		orderedPoints[1] = null;
		currentSelectionPoint = -1;
		if (actionModeStarted != null)
			actionModeStarted.finish();
		invalidate();
	}
	
	private void onNavigateLink(FDLink link) {
		//Log.i("ClickEvent", "onNavigateLink " + link.type + "  " + link.link);
		if (delegate != null) {
			delegate.endlessTextViewLinkActivated(link.type, link.link);
		}
	}

	public FDRecordLocation getHitLocation(float currX, float currY) {
		FDRecordLocation hr;
		hr = new FDRecordLocation();
		hr.x = currX;
		hr.y = currY;
		
		Log.i("ClickEvent", "Endless.onClick");
		for(FDRecordBase base : paintedRecords) {
			
			if (base.testHit(hr, paddingLeft)) {
				hr.path.add(0, base);
				Log.i("ClickEvent", "rec detected " + base.recordId);
				return hr;
			}
		}
		
		if (paintedRecords.size() > 0) {
			hr.record = paintedRecords.get(paintedRecords.size() - 1);
		}
		return hr;
	}

	
	public String getSelectedText(boolean b) {
		StringBuilder sb = new StringBuilder();
		int firstRecId = -1;
		if (orderedPoints[0] != null && orderedPoints[1] != null) {
			firstRecId = orderedPoints[0].record.recordId;
			for(int i = firstRecId; i <= orderedPoints[1].record.recordId; i++) {
				FDRecordBase rd = source.getRecord(i, this, -1);
				for(FDRecordPart part : rd.parts) {
					part.getSelectedText(sb);
				}
				sb.append('\n');
			}
		}
		
		if (b && firstRecId >= 0) {
			sb.append(String.format("\n[%s]\n\n", source.getRecordPath(firstRecId)));
		}
		
		return sb.toString();
	}
	
	public void onDragContent(float currX, float currY) {
		// finger is moving
		if (!disableTouchDrag) {
			lastDifferenceY  = (currY - lastY) * 2.0f;
			this.currentPos.offset += lastDifferenceY;
            yCurrTrace -= lastDifferenceY;
//			Log.i(DEBUG_TAG, "Coordinates: " + (currX - lastX) + " " + (currY - lastY));	
			invalidate();
		}
	}

	private void onDragSelection(float currX, float currY) {

		if (currentSelectionPoint >= 0) {
			
			processSelectionPoints(true);
//			Log.i("trans", String.format("A compare recIds: %d %d",
//					selectionPoints[0].record.recordId,
//					selectionPoints[1].record.recordId));
			FDRecordLocation testLocation = getHitLocation(currX, currY + currentHotSpotCorrection);

			// accepted are only those locations where PART is initialized
			if (testLocation.cell != null) {
				testLocation.selectionMarkOriginal = selectionPoints[currentSelectionPoint].selectionMarkOriginal;
				selectionPoints[currentSelectionPoint] = testLocation;
			}
//			Log.i("trans", String.format("B compare recIds: %d %d   %f %f %f  %s %d",
//					selectionPoints[0].record.recordId,
//					selectionPoints[1].record.recordId, currX, currY, currentHotSpotCorrection, Boolean.toString(selectionOrderNormal), currentSelectionPoint));
//			Log.i("ClickEvent", "onDragSelection areaType:" + selectionPoints[currentSelectionPoint].areaType);
			processSelectionPoints(false);
			invalidate();
		}
	}

	private int onLongClickStart(float currX, float currY) {
		FDRecordLocation hr = getHitLocation(currX, currY);
		
		if (hr.cell != null) {
			// if we have some selection 
			// we should clean it
			if (selectionPoints[0] != null) {
				processSelectionPoints(true);
			}
			
			// initial setting
			selectionPoints[0] = hr;
			selectionPoints[1] = hr.clone();
			currentSelectionPoint = 0;
			

			processSelectionPoints(false);
			invalidate();
			
			StartSelectionContext();
			
			return TRACK_DRAG_SELECTION;
		}
		return TRACK_NONE;
	}

	private void processSelectionPoints(boolean clearSelection) {

		if (selectionPoints[0] == null || selectionPoints[1] == null)
			return;

		boolean oneParaSel;
		int start;
		int end;
		
		// sort selection points
		oneParaSel = sortSelectionPoints();

		if (oneParaSel) {
			// if selection only within 1 paragraph
			// then we align selection to words
			start = orderedPoints[0].cellNum;
			end = orderedPoints[1].cellNum;
			
			FDParagraph para = orderedPoints[0].para;
			if (para != null) {
				if (clearSelection) {
					for(int i = start; i <= end; i++) {
						FDPartBase cell = para.parts.get(i);
						cell.selected = FDSelection.None;
					}
				} else {
					for(int i = start + 1; i < end; i++) {
						FDPartBase cell = para.parts.get(i);
						cell.selected = FDSelection.Middle;
					}
					if (start == end) {
						para.parts.get(start).selected = FDSelection.First | FDSelection.Last;
					} else {
						para.parts.get(start).selected = FDSelection.First;
						para.parts.get(end).selected = FDSelection.Last;
					}
				}
			}

		} else {
			// if selection covers more paragraphs
			// then we align selection to paragraph
			start = orderedPoints[0].record.recordId;
			end = orderedPoints[1].record.recordId;

			FDRecordBase rec;
			if (clearSelection) {
				
				rec = source.getRecord(start, this, 1);
				for(FDRecordPart part : rec.parts) {
					part.selected = FDSelection.None;
				}
				
				for(int i = start + 1; i < end; i++) {
					rec = source.getRecord(i, this, 1);
					for(FDRecordPart part : rec.parts) {
						part.selected = FDSelection.None;
					}
				}
				
				rec = source.getRecord(end, this, 1);
				for(FDRecordPart part : rec.parts) {
					part.selected = FDSelection.None;
				}
								
			} else {

				rec = source.getRecord(start, this, 1);
				for(FDRecordPart part : rec.parts) {
					if (part.orderNo == orderedPoints[0].partNum) {
						part.selected = FDSelection.First;
					} else if (part.orderNo > orderedPoints[0].partNum) {
						part.selected = FDSelection.Middle;
					}
				}
				
				for(int i = start + 1; i < end; i++) {
					rec = source.getRecord(i, this, 1);
					for(FDRecordPart part : rec.parts) {
						part.selected = FDSelection.Middle;
					}
				}
				
				rec = source.getRecord(end, this, 1);
				for(FDRecordPart part : rec.parts) {
					if (part.orderNo == orderedPoints[1].partNum) {
						if (part.selected == FDSelection.First)
							part.selected |= FDSelection.Last;
						else
							part.selected = FDSelection.Last;
					} else if (part.orderNo < orderedPoints[1].partNum) {
						part.selected = FDSelection.Middle;
					}
				}
			}
		}
		
		
		
	}

	public boolean sortSelectionPoints() {
		
		boolean oneParaSel;
//		Log.i("trans", String.format("compare recIds: %d %d  -- %d %d -- %d %d",
//				selectionPoints[0].record.recordId, selectionPoints[1].record.recordId,
//				selectionPoints[0].partNum, selectionPoints[1].partNum,
//				selectionPoints[0].cellNum, selectionPoints[1].cellNum));
		if (selectionPoints[0].record.recordId == selectionPoints[1].record.recordId) {
			// we are in 1 record and both are paragraphs
			if (selectionPoints[0].partNum == selectionPoints[1].partNum) {
				oneParaSel = true;
				// sorting within 1 para
				if (selectionPoints[0].cellNum > selectionPoints[1].cellNum) {
					sortSelectionReverseOrder();
				} else {
					sortSelectionNormalOrder();
				}
			} else {
				// different parts, we have to select whole parts
				oneParaSel = false;
				if (selectionPoints[0].partNum > selectionPoints[1].partNum) {
					sortSelectionReverseOrder();
				} else {
					sortSelectionNormalOrder();
				}
			}
		} else {
			oneParaSel = false;
			if (selectionPoints[0].record.recordId > selectionPoints[1].record.recordId) {
				sortSelectionReverseOrder();
			} else {
				sortSelectionNormalOrder();
			}
		}
		
		// setting 
		if (oneParaSel) {
			
		} else {
			
		}
		return oneParaSel;
	}

	public void sortSelectionNormalOrder() {
		selectionOrderNormal = true;
		orderedPoints[0] = selectionPoints[0];
		orderedPoints[1] = selectionPoints[1];
	}

	public void sortSelectionReverseOrder() {
		selectionOrderNormal = false;
		orderedPoints[0] = selectionPoints[1];
		orderedPoints[1] = selectionPoints[0];
	}

	public boolean movementIndicated(float currX, float currY) {
		return (Math.abs(currX - lastX) >= moveSensitivityX) || (Math.abs(currY - lastY) >= moveSensitivityY);
	}

	/**
	 * Must return next state
	 * 
	 * Default next state is WAIT_NEXT
	 * but if users clicked on startSelectionmark or endSelectionMark,
	 * then next status is different
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int onDownTouch(float x, float y) {

		float a;
		float dist = 1000;
		int indexStart = selectionOrderNormal ? 0 : 1;
		int indexEnd = 1 - indexStart;
		
		currentSelectionPoint = -1;
		
		if (selectionPoints[indexStart] != null) {
			a = Math.abs(x - selectionPoints[indexStart].hotSpotX)
					+ Math.abs(y - selectionPoints[indexStart].hotSpotY + selectionHotSpotOffsetY);
			if (a < dist && a < selectionHotSpotMaxDistance) {
				dist = a;
				currentSelectionPoint = indexStart;
				currentHotSpotCorrection = selectionHotSpotOffsetY;
				selectionPoints[indexStart].selectionMarkOriginal = FDSelection.First;
				selectionPoints[indexEnd].selectionMarkOriginal = FDSelection.Last;
			}
		}
		
		if (selectionPoints[indexEnd] != null) {
			a = Math.abs(x - selectionPoints[indexEnd].hotSpotX)
					+ Math.abs(y - selectionPoints[indexEnd].hotSpotY - selectionHotSpotOffsetY);
			if (a < dist && a < selectionHotSpotMaxDistance) {
				currentSelectionPoint = indexEnd;
				currentHotSpotCorrection = -selectionHotSpotOffsetY;
				selectionPoints[indexStart].selectionMarkOriginal = FDSelection.First;
				selectionPoints[indexEnd].selectionMarkOriginal = FDSelection.Last;
			}
		}

		if (currentSelectionPoint >= 0)
			return TRACK_DRAG_SELECTION;
		
		return TRACK_WAIT_NEXT;
	}
	
	public int getPaintingWidth() {
		return (int)(getMeasuredWidth() - paddingLeft - paddingRight);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		if (this.currentPos == null) {
			this.currentPos = new TextPosition();
		}

		int prevRec = this.currentPos.record;
		float yCurr = this.currentPos.offset;
		float partHeight = 0;
		int iRecord = this.currentPos.record;

		int height = getMeasuredHeight();
		int width = getPaintingWidth();
		
		//Log.i("currerce", "draw: " + prevRec);
		if (source == null) {
			//Log.i("draw", "source is null");
			return;
		}
		
		if (currentPaint == null) {
			currentPaint = new Paint();
			
			currentPaint.setColor(0x60000000);
			currentPaint.setTypeface(FDTypeface.getTypeface(FDTypeface.ARIAL_FONT, false, false));
			currentPaint.setTextSize(18);
		}

        //Log.i("draw", "  befor current Y = " + yCurrTrace);
        /*while(yCurrTrace > 0) {
            yCurrTrace -= edgeHeight;
        }

        while(yCurrTrace + edgeHeight < 0) {
            yCurrTrace += edgeHeight;
        }
        //Log.i("draw", "  after current Y = " + yCurrTrace);

        if (leftEdgeBitmap != null || rightEdgeBitmap != null) {
            float start = yCurrTrace;
            float widthStart = width - edgeWidth;
            float end = height;
            while(start < end) {
                if (leftEdgeBitmap != null)
                    canvas.drawBitmap(leftEdgeBitmap, 0, start, currentPaint);
                if (rightEdgeBitmap != null)
                    canvas.drawBitmap(rightEdgeBitmap, widthStart, start, currentPaint);
                start += edgeHeight;
            }
        }*/
		//Log.i("draw", "current Y = " + yCurr);
		
		while(yCurr > 0) {
			if (iRecord == 0) {
				yCurr = 0;
				//Log.i("draw", "reset to yCurr = 0");
				break;
			}
			iRecord --;
			FDRecordBase record = source.getRecord(iRecord, this, -1);
			if (record != null) {
				partHeight = record.ValidateForWidth(width);
				yCurr -= partHeight;
				//Log.i("draw", "moving base rec to " + iRecord);
			}
		}
		
		this.currentPos.offset = yCurr;
		this.currentPos.record = iRecord;

		// just for testing purpose
		int paraLimit = 99;
		
		paintedRecords.clear();

		FDDrawRecordContext context = new FDDrawRecordContext();
		context.width = width;

		// reset selection points
		if (selectionPoints != null) {
			for (FDRecordLocation loc : selectionPoints) {
				if (loc != null)
					loc.selectionMarkType = FDSelection.None;
			}
		}

		//Log.i("drawy", "width is " + width);
		while(yCurr < height) {
			FDRecordBase record = source.getRecord(iRecord, this, 1);

			if (record != null) {
				paintedRecords.add(record);
				partHeight = record.ValidateForWidth(width);
				//Log.i("drawy", "height " + partHeight + " for record" + record.recordId);
				context.record = record;
				context.yCurr = yCurr;
				drawRecord(canvas, context);
			} else {
				partHeight = 1;
			}
			
			yCurr += partHeight;
			if (yCurr < 0) {
				this.currentPos.record = iRecord + 1;
				this.currentPos.offset = yCurr;
                yCurrTrace = 0;
			}
			iRecord++;
			
			// just for testing purpose
			paraLimit--;
			if (paraLimit < 1)
				break;
		}

		// drawing selection handler marks at positions of selection boundaries
		// in case selection mark is not selected, it is drawn on position fo boudnary
		// if selection mark is currently moved, it is drawn on position where user drags it
		if (selectionPoints != null) {
			int index = 0;
			for (FDRecordLocation point : selectionPoints) {
				if (point == null)
					continue;
				int type = (trackingMode == TRACK_DRAG_SELECTION ? point.selectionMarkOriginal : point.selectionMarkType);
				if (type == FDSelection.First) {
					if (index == currentSelectionPoint && trackingMode == TRACK_DRAG_SELECTION) {
						canvas.drawBitmap(bmpSelMarkA, lastX - selectionHotSpotOffsetX,
								lastY - selectionHotSpotOffsetX, currentPaint);
					} else {
						canvas.drawBitmap(bmpSelMarkA, point.hotSpotX - selectionHotSpotOffsetX,
								point.hotSpotY - selectionHotSpotOffsetX - selectionHotSpotOffsetY, currentPaint);
					}
				} else if (type == FDSelection.Last) {
					if (index == currentSelectionPoint && trackingMode == TRACK_DRAG_SELECTION) {
						canvas.drawBitmap(bmpSelMarkB, lastX - selectionHotSpotOffsetX,
								lastY - selectionHotSpotOffsetY, currentPaint);
					} else {
						canvas.drawBitmap(bmpSelMarkB, point.hotSpotX - selectionHotSpotOffsetX,
								point.hotSpotY, currentPaint);
					}
				}
				index++;
			}
		}

		if (delegate != null) {
			delegate.endlessTextLoading(context.loading);
		}

		// if record number changed, notify delegate
		if (prevRec != currentPos.record) {
			if (delegate != null) {
				delegate.endlessTextViewRecordChanged(this.currentPos.record);
			}
		}
		
		handleFadeScrolling();
	}

	/**
	 * This is handling of faded scrolling
	 * We gradually decrease the speed of scrolling.
	 * Finally if scroll is lower than 1.5 pixel, we stop fading.
	 */
	
	public void handleFadeScrolling() {
		if (fadeScrolling) {
			if (lastDifferenceY > 1.5) {
				if (lastDifferenceY > 120) {
					lastDifferenceY = 120;
				}
				lastDifferenceY *= 0.8;
				this.currentPos.offset += lastDifferenceY;
                yCurrTrace -= lastDifferenceY;
				invalidate();
			} else if (lastDifferenceY < -1.5) {
				if (lastDifferenceY < -120) {
					lastDifferenceY = -120;
				}
				lastDifferenceY *= 0.8;
				this.currentPos.offset += lastDifferenceY;
                yCurrTrace -= lastDifferenceY;
				invalidate();
			} else {
				lastDifferenceY = 0;
				fadeScrolling = false;
			}
		}
	}

	private void drawRecord(Canvas canvas, FDDrawRecordContext recon) {

		if (drawLineBeforeRecord) {
			canvas.drawLine(0, recon.yCurr, recon.width + paddingLeft + paddingRight, recon.yCurr, currentPaint);
		}
		if (drawRecordNumber) {
			canvas.drawText(String.format("%d", recon.record.recordId), 5, recon.yCurr + 30, currentPaint);
		}

		FDDrawTextContext context = new FDDrawTextContext();
		context.orderedPoints = orderedPoints;

		if (recon.record.loading) {
			recon.loading = true;
			//canvas.drawText("Loading...", paddingLeft + 20, recon.yCurr + 25, notePaint);
		} else {
			float x = paddingLeft;
			float y = recon.yCurr;
			int order = 0;
            boolean canHaveNotes = source.canHaveNotes();
			VBCustomNotes notes = source.recordNotesForRecord(recon.record.recordId);
            VBCustomHighlights highs = source.highlightersForRecord(recon.record.recordId);


			for(FDRecordPart rp : recon.record.parts) {
				rp.orderNo = order;
				rp.absoluteTop = y;
				
				context.highsAnchor = (highs != null) ? highs.anchorForKey(order) : null;
				context.xStart = x;
				context.yStart = y;

				y += rp.draw(canvas, context);
				rp.absoluteBottom = y;
				order++;
			}

			// drawing note icon
            if ((y > (recon.yCurr + noteBitmap.getHeight())) && canHaveNotes) {
                if ((notes != null) && notes.hasText())
                    canvas.drawBitmap(noteBitmap, 10, recon.yCurr + 4, currentPaint);
                else
                    canvas.drawBitmap(noteBitmapX, 10, recon.yCurr + 4, currentPaint);
            }

			// drawing visited icon
            if (y > recon.yCurr + 32) {
                if (recon.record.visited) {
                    Rect targetRect = new Rect();
                    targetRect.left = (int)(recon.width + paddingLeft + 10);
                    targetRect.top = (int)(recon.yCurr + 12);
                    targetRect.right = (int)(recon.width + paddingLeft + 42);
                    targetRect.bottom = (int)(recon.yCurr + 44);
                    canvas.drawBitmap(visitedBitmap, null, targetRect, currentPaint);
                }
            }

		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int measuredHeight = measureHeight(heightMeasureSpec);
		int measuredWidth = measureWidth(widthMeasureSpec);

		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	private int measureWidth(int measureSpec) {
		//int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		
		//Log.i("cview", "mw.mode: " + specMode + " measureWidth returns " + specSize);
		return specSize;
	}
	
	private int measureHeight(int measureSpec) {
		//int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		
		//Log.i("cview", "measureHeight returns " + specSize);
		return specSize;		
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		Log.i(DEBUG_TAG, "onDown: " + arg0.toString());
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		Log.i(DEBUG_TAG, "onFling: " + arg0.toString());
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		Log.i(DEBUG_TAG, "onLongPress: " + arg0.toString());
		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		//Log.i(DEBUG_TAG, "onScroll: " + arg0.toString() + ", m2: " + arg1.toString() + ", diffX: " + arg2 + ", diffY: " + arg3);
		this.currentPos.offset += arg3;
        yCurrTrace -= arg3;
		invalidate();
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		Log.i(DEBUG_TAG, "onShowPress: " + arg0.toString());
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		Log.i(DEBUG_TAG, "onSingleTapUp: " + arg0.toString());
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		Log.i(DEBUG_TAG, "onDoubleTap: " + e.toString());
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		Log.i(DEBUG_TAG, "onDoubleTapEvent: " + e.toString());
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		Log.i(DEBUG_TAG, "onSingleTapConfirmed: " + e.toString());
		return false;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		Log.i(DEBUG_TAG, "onScale: " + detector.toString());
		return false;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		Log.i(DEBUG_TAG, "onScaleBegin: " + detector.toString());
		return false;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		Log.i(DEBUG_TAG, "onScaleEnd: " + detector.toString());
		
	}

	public void setRecord(int record) {
		this.currentPos = new TextPosition();
		this.currentPos.record = record;
		this.currentPos.offset = 0;
        yCurrTrace = 0;
		if (history == null) {
			history = new ArrayList<TextPosition>();
		}
		while(history.size() > historyPos + 1) {
			history.remove(historyPos + 1);
		}
		this.history.add(this.currentPos);
		historyPos = history.size() - 1;
		if (delegate != null) {
			delegate.endlessTextHistoryChanged();
		}
		invalidate();
	}
	
	public int getCurrentRecord()
	{
		if (currentPos != null)
			return currentPos.record;
		return 0;
	}
	
	public boolean canGoBack()
	{
		if (history == null)
			return false;
		return (historyPos > 0);
	}
	
	public boolean canGoForward()
	{
		if (history == null)
			return false;
		return historyPos < (history.size() - 1);
	}
	
	public void goBack()
	{
		if (historyPos > 0) {
			historyPos --;
			currentPos = history.get(historyPos);
			if (delegate != null) {
				delegate.endlessTextHistoryChanged();
			}
			invalidate();
		}
	}
	
	public void goForward()
	{
		if (historyPos < (history.size() - 1)) {
			historyPos++;
			currentPos = history.get(historyPos);
			if (delegate != null) {
				delegate.endlessTextHistoryChanged();
			}
			invalidate();
		}
	}
	

	public FlatFileSourceInterface getSource() {
		return source;
	}

	public void setSource(FlatFileSourceInterface source) {
		this.source = source;
	}

	@Override
	public void RecordPageLoaded(int page, int direction) {

		int width = getPaintingWidth();
		float newHeight = 0;
		//int mode = 0;
		// first check is what type of recalculation it is
		for(FDRecordBase record : paintedRecords) {
			if (record.requestedAlign < 0) {
				newHeight = record.ValidateForWidth(width);
				this.currentPos.offset -= (newHeight - FDRecordBase.loadingRecordHeight);
                yCurrTrace = 0;
			}
			record.requestedAlign = 0;
		}
		
		invalidate();
	}

	@Override
	public boolean onActionItemClicked(ActionMode arg0, MenuItem arg1) {
		
		ClipboardManager clipboard = null;
		ClipData clip = null;
		if (arg1.getItemId() == R.id.ecm_copy) {
			clipboard = (ClipboardManager)MainActivity.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
			clip = ClipData.newPlainText("Vedabase Clip", getSelectedText(false));
			//Log.i("targ", "Selected: " + getSelectedText(false));
			clipboard.setPrimaryClip(clip);
		} else if (arg1.getItemId() == R.id.ecm_copy_ref) {
			clipboard = (ClipboardManager)MainActivity.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
			clip = ClipData.newPlainText("Vedabase Clip", getSelectedText(true));
			clipboard.setPrimaryClip(clip);
		} else if (arg1.getItemId() == R.id.hglt_0_yellow) {
			setHighlightToSelectedText(0);
			invalidate();
		} else if (arg1.getItemId() == R.id.hglt_1_green) {
			setHighlightToSelectedText(1);
			invalidate();
		} else if (arg1.getItemId() == R.id.hglt_2_cyan) {
			setHighlightToSelectedText(2);
			invalidate();
		} else if (arg1.getItemId() == R.id.hglt_3_red) {
			setHighlightToSelectedText(3);
			invalidate();
		} else if (arg1.getItemId() == R.id.hglt_4_magenta) {
			setHighlightToSelectedText(4);
			invalidate();
		} else if (arg1.getItemId() == R.id.hglt_5_orange) {
			setHighlightToSelectedText(5);
			invalidate();
		} else if (arg1.getItemId() == R.id.hglt_6_pink) {
			setHighlightToSelectedText(6);
			invalidate();
		} else if (arg1.getItemId() == R.id.hglt_7_blue) {
			setHighlightToSelectedText(7);
			invalidate();
		} else if (arg1.getItemId() == R.id.hglt_c_clear) {
			setHighlightToSelectedText(-1);
			invalidate();
		} else {
		}
		return false;
	}

    public ArrayList<FDRecordBase> getSelectedRecordList() {
        ArrayList<FDRecordBase> list = new ArrayList<FDRecordBase>();
        if (orderedPoints[0] != null && orderedPoints[1] != null) {
            for (int i = orderedPoints[0].record.recordId;
                 i <= orderedPoints[1].record.recordId; i++) {
                list.add(source.getRecord(i, this, -1));
            }
        }
        return list;
    }

	public void setHighlightToSelectedText(int highlighterId) {
		
        for(FDRecordBase rd : getSelectedRecordList()) {
            VBCustomHighlights notes = source.safeHighlightersForRecord(rd.recordId);
            if (notes != null) {
                setHighlighterToRecordParts(highlighterId, notes, rd);
                notes.setHighlightedText(retrieveHighlightedText(notes, rd));
            }
		}
		
		if (delegate != null) {
			delegate.endlessHighlightsChanged();
		}
	}

    private void setHighlighterToRecordParts(int highlighterId, VBCustomHighlights notes, FDRecordBase rd) {
        VBHighlighterAnchor anchor;
        int partNo = 0;
        for(FDRecordPart part : rd.parts) {
            if (!part.hasSelection())
                continue;

            anchor = notes.safeAnchorForKey(partNo);
            if (part.selected != FDSelection.None) {
                anchor.setHighlighterRange(0, part.parts.size(), highlighterId);
            } else {
                for(int j = 0; j < part.parts.size(); j++) {
                    if (part.parts.get(j).selected == FDSelection.None)
                        continue;
                    anchor.setHighlighter(j, highlighterId);
                }
            }
            partNo++;
        }
    }

    private String retrieveHighlightedText(VBCustomHighlights notes, FDRecordBase rd) {
        int partNo = 0;
        VBHighlighterAnchor anchor;
        StringBuilder sb = new StringBuilder();

        for(FDRecordPart part : rd.parts) {
            anchor = notes.anchorForKey(partNo);
            if (anchor != null) {
                boolean needDots = false;
                for(int k = 0; k < part.parts.size(); k++) {
                    FDPartBase cell = part.parts.get(k);
                    if (anchor.getHighlighterAtPos(k) >= 0) {
                        sb.append(cell.toString());
                        needDots = true;
                    } else {
                        if (needDots) {
                            sb.append("... ");
                        }
                        needDots = false;
                    }
                }
            }
            partNo++;
        }

        return sb.toString();
    }

    @Override
	public boolean onCreateActionMode(ActionMode arg0, Menu arg1) {
		actionModeStarted = arg0;
		arg0.setTitle("Selection");
		arg0.getMenuInflater().inflate(R.menu.endless_context_menu, arg1);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode arg0) {
		actionModeStarted = null;
		EndSelectionContext();
	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
		return false;
	}
	
	
	
	
}
