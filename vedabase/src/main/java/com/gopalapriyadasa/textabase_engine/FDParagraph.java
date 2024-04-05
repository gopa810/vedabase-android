package com.gopalapriyadasa.textabase_engine;

import java.util.ArrayList;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

public class FDParagraph extends FDRecordPart {

	public float layoutWidth = 0;
	public float layoutHeight = 0;
	public ArrayList<FDParagraphLine> lines = new ArrayList<FDParagraphLine>();

	public float borderTop = -1;
	public float borderBottom = -1;
	public float borderLeft = -1;
	public float borderRight = -1;
	
	public float clientTop = -1;
	
	public FDParagraph() {
	}

	@Override
	public float ValidateForWidth(int width) {

		//Log.i("drawy", "ok");
		borderLeft = paraFormat.getMargin(FDParaFormat.SIDE_LEFT) 
				+ paraFormat.getBorderWidth(FDParaFormat.SIDE_LEFT) / 2;
		float left = borderLeft 
				+ paraFormat.getBorderWidth(FDParaFormat.SIDE_LEFT)/2
				+ paraFormat.getPadding(FDParaFormat.SIDE_LEFT);
		borderRight = width - paraFormat.getMargin(FDParaFormat.SIDE_RIGHT) 
				- paraFormat.getBorderWidth(FDParaFormat.SIDE_RIGHT)/2;
		float right = borderRight 
				- paraFormat.getBorderWidth(FDParaFormat.SIDE_RIGHT)/2
				- paraFormat.getPadding(FDParaFormat.SIDE_RIGHT);
		borderTop = (paraFormat.getMargin(FDParaFormat.SIDE_TOP) 
				+ paraFormat.getBorderWidth(FDParaFormat.SIDE_TOP)/2) * FDCharFormat.getMultiplySpaces();
		float top = borderTop 
				+ paraFormat.getBorderWidth(FDParaFormat.SIDE_TOP)/2
				+ paraFormat.getPadding(FDParaFormat.SIDE_TOP);
		clientTop = top;
		
		float borderBottomAdd = paraFormat.getPadding(FDParaFormat.SIDE_BOTTOM) + paraFormat.getBorderWidth(FDParaFormat.SIDE_BOTTOM) / 2;
		float bottomAdd = (paraFormat.getBorderWidth(FDParaFormat.SIDE_BOTTOM) / 2 
				+ paraFormat.getMargin(FDParaFormat.SIDE_BOTTOM))* FDCharFormat.getMultiplySpaces();
		
		//Log.i("drawe", String.format("[Para] top %f, bottom %f", top, borderBottomAdd + bottomAdd));
		// width is total width of paragraph
		// we have to subtract left border width, padding and margin
		// the same for right border width, padding and margin
		lines.clear();
		FDParagraphLine currentLine = new FDParagraphLine(this);
		lines.add(currentLine);
		float currentWidth = left + paraFormat.firstIndent /* FDCharFormat.getMultiplyFontSize()*/;
		layoutWidth = width;
		Rect bounds = new Rect();
		
		FontMetrics fm = new FontMetrics();
		Paint lastPaint = null;
		//
		// first step: distribute elements to lines
		//
		int order = 0;
		for(FDPartBase part : parts) {
			part.orderNo = order;
			if (part instanceof FDPartString) {
				// string element
				FDPartString strp = (FDPartString)part;
				if (strp.hidden) {
					strp.desiredWidth = 0;
				} else {
					lastPaint = strp.format;
					strp.format.getFontMetrics(fm);
					//Log.i("drawe", String.format("[Format] top:%f, bottom:%f, ascent:%f", fm.top, fm.bottom, fm.ascent));
					strp.format.getTextBounds(strp.text, 0, strp.text.length(), bounds);
					//Log.i("drawe", String.format("[Height] %f", strp.format.getTextSize()));
					strp.desiredWidth = bounds.width();
					strp.desiredHeight = bounds.height();
				}
				if (strp.desiredWidth + currentWidth > right) {
					currentLine = new FDParagraphLine(this);
					lines.add(currentLine);
					currentWidth = left;
				}
				currentLine.parts.add(strp);
				currentLine.mergeTop(-fm.ascent + fm.leading);
				currentLine.mergeBottom(-fm.descent);
				
				currentWidth += strp.desiredWidth;
				currentLine.width = currentWidth;
				
			} else if (part instanceof FDPartSpace) {
				// space element
				FDPartSpace spc = (FDPartSpace)part;
				if (spc.breakLine) {
					currentLine.parts.add(spc);
					currentLine = new FDParagraphLine(this);
					lines.add(currentLine);
					currentWidth = left;
					spc.desiredWidth = 0;
				} else {
					if (lastPaint != null) {
						spc.desiredWidth = lastPaint.measureText(" ");
					} else {
						spc.desiredWidth = 14;
					}
					spc.desiredHeight = 0;
					currentLine.parts.add(spc);
				}
				currentWidth += spc.desiredWidth;
				currentLine.width = currentWidth;
			} else if (part instanceof FDPartImage) {
				// image element
				FDPartImage img = (FDPartImage)part;
				if (img.desiredWidth + currentWidth > right) {
					currentLine = new FDParagraphLine(this);
					lines.add(currentLine);
					currentWidth = left;
				}
				currentLine.parts.add(img);
				Log.i("drawq", "topOffset: " + currentLine.topOffset + " img.height");
				currentLine.mergeTop(img.desiredHeight);
				Log.i("drawq", "topOffset after: " + currentLine.topOffset);
				
				currentWidth += img.desiredWidth;
				currentLine.width = currentWidth;
			}
			part.parentLine = currentLine;
			order++;
		}
		
		// currentLine contains last line
		
		//
		// second step: determine line heights
		//
		float start = top;
		//float prevLineBottom = top;
		float startX = left + paraFormat.firstIndent;
		for(FDParagraphLine line : lines) {
			// each line has topOffset and bottomOffset
			// when painting the paragraph, we start at offsetY = 0
			// we add line's height and draws text there
			// then we add line's subheight so next line will start with adding its height

			// line's height is calculated as topOffset * lineHeight (from para formatting)
			// line's subheight is bottomOffset
			
			// therefore we just multiply topOffset with lineHeight
			//Log.i("drawe", String.format("line.topOffset: %f, paraFormat.lineHeight: %f", line.topOffset, paraFormat.lineHeight));
			if (paraFormat.lineHeight > 0.3) {
				line.topOffset *= paraFormat.lineHeight * FDCharFormat.getMultiplySpaces();
			}
			line.startOffsetY = start + line.topOffset;
			line.startOffsetX = startX;
			startX = left;
			//Log.i("drawe", "line.startOffsetY:" + line.startOffsetY);
			line.height = (line.topOffset - line.bottomOffset * FDCharFormat.getMultiplySpaces());
			//line.height = start - prevLineBottom;
			start += line.height;
		}
		
		float workWidth = right;
		if (paraFormat.align == FDParaFormat.ALIGN_CENTER) {
			for(FDParagraphLine line : lines) {
				line.startOffsetX += (workWidth - line.width) / 2;
				workWidth = right;
			}
		} else if (paraFormat.align == FDParaFormat.ALIGN_RIGHT) {
			for(FDParagraphLine line : lines) {
				line.startOffsetX += (workWidth - line.width);
				workWidth = right;
			}			
		} else if (paraFormat.align == FDParaFormat.ALIGN_JUST) {
			for(FDParagraphLine line : lines) {
				// currentLine contains last line added to the para
				// and when alignment is JUSTIFY, then we dont align
				// last line
				if (line == currentLine)
					break;
				
				// we need to add some width to each space in the line
				float addWidth = (workWidth - line.width);
				if (addWidth < 0) {
					Log.i("drawe", "addWidth = " + addWidth + "  record: ");
					addWidth = 0;
				}
				int spaces = 0;
				boolean lastIsSpace = false;
				for(FDPartBase part : line.parts) {
					lastIsSpace = false;
					if (part instanceof FDPartSpace) {
						FDPartSpace spc = (FDPartSpace)part;
						if (!spc.breakLine) {
							spaces++;
							lastIsSpace = true;
						} else {
							// if last is <CR>
							// then we dont need adjust space widths
							// we can accomplish this by setting 0 spaces count
							lastIsSpace = false;
							spaces = 0;
							break;
						}
					}
				}
				
				if (lastIsSpace)
					spaces--;

				float originalWidth = 0.0f;
				if (spaces > 0) {
					for(FDPartBase part : line.parts) {
						if (part instanceof FDPartSpace) {
							FDPartSpace spc = (FDPartSpace)part;
							if (!spc.breakLine) {
								originalWidth = spc.desiredWidth;
								spc.desiredWidth += (addWidth / spaces);
								if (spc.desiredWidth > originalWidth*2.5f)
									spc.desiredWidth = originalWidth*2.5f;
							}
						}
					}
				}
				workWidth = right;
			}			
		}
		
		// we adjust bottom dimensions
		borderBottom = start + borderBottomAdd;
		layoutHeight = borderBottom + bottomAdd;
		
		return layoutHeight;
	}

	@Override
	public float draw(Canvas canvas, FDDrawTextContext context) {

		// float xStart, float yStart, FDRecordLocation [] orderedPoints, VBHighlighterAnchor anchor
		float x = context.xStart, y = context.yStart;
		Paint p;
		Paint selp = paraFormat.getSelectionPaint();
		Paint selpline = paraFormat.getSelectionLinePaint();

		if (context.highsAnchor != null) {
			Log.i("high", "not null anch");
		}
		// painting border
		if (selected != FDSelection.None) {
			canvas.drawRect(context.xStart, context.yStart, context.xStart + layoutWidth, context.yStart + layoutHeight, selp);
			if ((selected & FDSelection.First) != 0) {
				canvas.drawLine(context.xStart, context.yStart, context.xStart + layoutWidth, context.yStart, selpline);
				//canvas.drawCircle(context.selectionMarkA.x, context.selectionMarkA.y, 4, selpline);
				if (context.orderedPoints[0] != null) {
					context.orderedPoints[0].hotSpotX = context.xStart + layoutWidth/2;
					context.orderedPoints[0].hotSpotY = context.yStart;
					context.orderedPoints[0].selectionMarkType = FDSelection.First;
				}
			}
			if ((selected & FDSelection.Last) != 0) {
				canvas.drawLine(context.xStart,  context.yStart + layoutHeight, context.xStart + layoutWidth, context.yStart + layoutHeight, selpline);
				//canvas.drawCircle(context.selectionMarkB.x,context.selectionMarkB.y, 4, selpline);
				if (context.orderedPoints[1] != null) {
					context.orderedPoints[1].hotSpotX = context.xStart + layoutWidth/2;
					context.orderedPoints[1].hotSpotY = context.yStart + layoutHeight;
					context.orderedPoints[1].selectionMarkType = FDSelection.Last;
				}
			}
		} else {
			p = paraFormat.getBackgroundPaint();
			if (p != null)
				canvas.drawRect(context.xStart + borderLeft, context.yStart + borderTop, context.xStart + borderRight, context.yStart + borderBottom, p);
		}
		p = paraFormat.getLinePaintForBorderSide(FDParaFormat.SIDE_LEFT);
		if (p != null)
			canvas.drawLine(context.xStart + borderLeft, context.yStart + borderTop, context.xStart + borderLeft, context.yStart + borderBottom, p);
		p = paraFormat.getLinePaintForBorderSide(FDParaFormat.SIDE_RIGHT);
		if (p != null)
			canvas.drawLine(context.xStart + borderRight, context.yStart + borderTop, context.xStart + borderRight, context.yStart + borderBottom, p);
		p = paraFormat.getLinePaintForBorderSide(FDParaFormat.SIDE_TOP);
		if (p != null)
			canvas.drawLine(context.xStart + borderLeft, context.yStart + borderTop, context.xStart + borderRight, context.yStart + borderTop, p);
		p = paraFormat.getLinePaintForBorderSide(FDParaFormat.SIDE_BOTTOM);
		if (p != null)
			canvas.drawLine(context.xStart + borderLeft, context.yStart + borderBottom, context.xStart + borderRight, context.yStart + borderBottom, p);
		
		for(FDParagraphLine line : lines) {
			//Log.i("drawe", String.format("[DrawLine] bottomOffset:%f  startOffsetX:%f startOffsetY:%f topOffset:%f width:%f bottomOffset:%f",
			//		line.bottomOffset, line.startOffsetX, line.startOffsetY, line.topOffset, line.width, line.bottomOffset));
			x = context.xStart + line.startOffsetX;
			y = context.yStart + line.startOffsetY;
			int highlighterId = -1;
			for (FDPartBase part : line.parts) {
				if (context.highsAnchor != null) {
					//Log.i("high", "part.orderNo = " + part.orderNo);
					highlighterId = context.highsAnchor.getHighlighterAtPos(part.orderNo);
				}
				
				if (part instanceof FDPartString) {
					// string element
					FDPartString strp = (FDPartString)part;
					if (strp.desiredWidth > 0) {
						drawHighlighter(canvas, x, y, highlighterId, line, strp);
						drawSelectionBackground(canvas, context, x, y,
								selp, selpline, line, strp);
						drawTextBackground(canvas, context.orderedPoints, x, y,
								strp.backgroundColor, line, strp);
						canvas.drawText(strp.text, x, y, strp.format);
						x += strp.desiredWidth;
					}
					//Log.i("drawe", "text color = " + strp.format.getColor());
					//Log.i("drawe", String.format("stringPart x,y,text: %f,%f,%s  height, width %f,%f", x, y, strp.text,
					//		strp.desiredHeight, strp.desiredWidth));
				} else if (part instanceof FDPartSpace) {
					// space element
					FDPartSpace spc = (FDPartSpace)part;
					if (!spc.breakLine) {
						drawHighlighter(canvas, x, y, highlighterId, line, spc);
						if (selected == FDSelection.None && spc.selected != FDSelection.None) {
							drawSelectionBackground(canvas, context, x, y,
								selp, selpline, line, spc);
						//Log.i("high2", "space selected, highId = " + highlighterId);
						}
						drawTextBackground(canvas, context.orderedPoints, x, y, spc.backgroundColor, line, spc);
						x += spc.desiredWidth;
					}
				} else if (part instanceof FDPartImage) {
					// image element
					FDPartImage img = (FDPartImage)part;
					// draw image
					if (img.bitmap != null)
						canvas.drawBitmap(img.bitmap, x, y - img.desiredHeight, img.format);
					x += img.desiredWidth;
				}
			}
			
		}
		
		return layoutHeight;
	}

	private void drawTextBackground(Canvas canvas,
			FDRecordLocation[] orderedPoints, float x, float y, int bkgColor,
			FDParagraphLine line, FDPartSized strp) {

		if (bkgColor != 0) {
			float x1 = x+1;
			float x2 = x + strp.desiredWidth + 1;
			float y1 = y - line.topOffset;
			float y2 = y - line.topOffset + line.height;
			canvas.drawRect(x1, y1, x2, y2, FDParaFormat.getPaintForBackgroundColor(bkgColor));
		}
	}

	public void drawHighlighter(Canvas canvas, float x, float y, int highlighterId, FDParagraphLine line, FDPartSized strp) {
		
		
		Paint p = VBHighlighterAnchor.getPaint(highlighterId);
		if (p != null) {
			float y1 = y - line.topOffset;
			float y2 = y1 + line.height;
			//Log.i("ClickEvent", String.format("selected rect %f,%f,%f,%f", x1, y1, x2, y2));
			canvas.drawRect(x + 1, y1, x + strp.desiredWidth + 1, y2, p);
		}
	}
	
	public void drawSelectionBackground(Canvas canvas,
			FDDrawTextContext context, float x, float y, Paint selp,
			Paint selpline, FDParagraphLine line, FDPartSized strp) {
		if (selected == FDSelection.None && strp.selected != FDSelection.None) {
			float x1 = x+1;
			float x2 = x + strp.desiredWidth + 1;
			float y1 = y - line.topOffset;
			float y2 = y - line.topOffset + line.height;
			//Log.i("ClickEvent", String.format("selected rect %f,%f,%f,%f", x1, y1, x2, y2));
			canvas.drawRect(x1, y1, x2, y2, selp);
			if ((strp.selected & FDSelection.First) != 0) {
				canvas.drawLine(x1, y1, x1, y2, selpline);
				//canvas.drawCircle(x1, y1 - 4, 4, selpline);
				if (context.orderedPoints[0] != null) {
					context.orderedPoints[0].hotSpotX = x1;
					context.orderedPoints[0].hotSpotY = y1 - 4;
					context.orderedPoints[0].selectionMarkType = FDSelection.First;
				}
			}
			if ((strp.selected & FDSelection.Last) != 0) {
				canvas.drawLine(x2,  y1, x2, y2, selpline);
				//canvas.drawCircle(x2, y2 + 4, 4, selpline);
				if (context.orderedPoints[1] != null) {
					context.orderedPoints[1].hotSpotX = x2;
					context.orderedPoints[1].hotSpotY = y2 + 4;
					context.orderedPoints[1].selectionMarkType = FDSelection.Last;
				}
			}
		}
	}

	@Override
	public void testHit(FDRecordLocation hr, float paddingLeft) {
		float currY = absoluteTop + clientTop;
		float currX = 0;
		int orderLine = 0;
		//int orderPart = 0;
		for(FDParagraphLine line : lines) {
			currX = line.startOffsetX + paddingLeft;
			line.orderNo = orderLine;
			//Log.i("ClickEvent", "line.bottomOffset: " + line.bottomOffset + "  hr.y:" + hr.y);
			//Log.i("CickEvent", "curr:" + currY + "  line.height:" + line.height);
			if (currY <= hr.y && (currY + line.height) > hr.y) {
				//Log.i("ClickEvent", "hitLine");
				for(FDPartBase part : line.parts) {
					//Log.i("ClickEvent", String.format("part(currY,+width):%f,%f", currX, currX + part.getWidth()));
					if (/*hr.x >= currY &&*/ hr.x < (currX + part.getWidth())) {
						//Log.i("ClickEvent", "hit part");
						testHitInitWithPart(hr, part);
						return;
					}
					currX += part.getWidth();
				}
				
				if (line.parts.size() > 0) {
					testHitInitWithPart(hr, line.parts.get(line.parts.size() - 1));
				}

				break;
			}
			currY += line.height;
			orderLine++;
		}
	}

	@Override
	public void getSelectedText(StringBuilder sb) {
		for(FDPartBase part : parts) {
			if ((part.selected | selected) == FDSelection.None)
				continue;
			
			if (part instanceof FDPartString) {
				FDPartString ps = (FDPartString)part;
				sb.append(ps.text);
			} else if (part instanceof FDPartSpace) {
				FDPartSpace sp = (FDPartSpace)part;
				if (sp.breakLine) {
					sb.append('\n');
				} else {
					sb.append(' ');
				}
			} else {
				sb.append(' ');
			}
		}
		super.getSelectedText(sb);
	}

	public void testHitInitWithPart(FDRecordLocation hr, FDPartBase part) {
		hr.path.add(part);
		hr.cell = part;
		hr.cellNum = part.orderNo;
		hr.partNum = this.orderNo;
		hr.areaType = FDRecordLocation.AREA_PARA;
		hr.para = this;
	}
}
