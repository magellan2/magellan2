/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

/* =======================================
 * JFreeChart : a Java Chart Class Library
 * =======================================
 * Version:         0.5.6;
 * Project Lead:    David Gilbert (david.gilbert@bigfoot.com);
 *
 * File:            VerticalBarPlot.java
 * Author:          David Gilbert;
 * Contributor(s):  -;
 *
 * (C) Copyright 2000, Simba Management Limited;
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307, USA.
 *
 * $Id: VerticalBarPlot.java 171 2004-04-06 07:45:45Z pavkovic $
 */
package magellan.client.skillchart;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.jrefinery.chart.Axis;
import com.jrefinery.chart.AxisNotCompatibleException;
import com.jrefinery.chart.Bar;
import com.jrefinery.chart.BarPlot;
import com.jrefinery.chart.CategoryAxis;
import com.jrefinery.chart.CategoryDataSource;
import com.jrefinery.chart.DataSource;
import com.jrefinery.chart.DataSources;
import com.jrefinery.chart.HorizontalAxis;
import com.jrefinery.chart.JFreeChart;
import com.jrefinery.chart.NumberAxis;
import com.jrefinery.chart.Plot;
import com.jrefinery.chart.VerticalAxis;
import com.jrefinery.chart.VerticalNumberAxis;
import com.jrefinery.chart.VerticalValuePlot;

/**
 * A Plot that displays data in the form of a bar chart, using data from any class that implements
 * the CategoryDataSource interface.
 *
 * @see Plot
 * @see CategoryDataSource I (Ulrich Küster) needed to change this class as I couldn't extend it,
 * 		because the method I needed to change (draw) accesses a private method (getBars). I
 * 		added     the method getBarAreas() to receive a Vector with the areas of the bars on the
 * 		screen,     that is needed in SkillChartJFreeChartPanel to show tooltips on them.
 */
public class VerticalBarPlot extends BarPlot implements VerticalValuePlot {
	/**
	 * This list is filled whenever the plot is drawn and holds the coordinates of all Bars as
	 * Rectangle2D objects. This data is used for tooltips in JFreeChartPanel
	 */
	private Vector<Rectangle2D> barAreas = new Vector<Rectangle2D>();

	/**
	 * Standard constructor: returns a BarPlot with attributes specified by the caller.
	 *
	 * @param chart The chart that the plot belongs to;
	 * @param horizontal The horizontal axis;
	 * @param vertical The vertical axis;
	 * 
	 * @param introGap The gap before the first bar in the plot;
	 * @param trailGap The gap after the last bar in the plot;
	 * @param categoryGap The gap between the last bar in one category and the first bar in the
	 * 		  next category;
	 * @param seriesGap The gap between bars within the same category.
	 *
	 * @throws AxisNotCompatibleException DOCUMENT-ME
	 */
	public VerticalBarPlot(JFreeChart chart, Axis horizontal, Axis vertical, Insets insets,
						   int introGap, int trailGap, int categoryGap, int seriesGap)
					throws AxisNotCompatibleException
	{
		super(chart, horizontal, vertical, insets, introGap, trailGap, categoryGap, seriesGap);
	}

	/**
	 * Standard constructor - builds a VerticalBarPlot with mostly default attributes.
	 *
	 * @param chart The chart that the plot belongs to;
	 * @param horizontalAxis The horizontal axis;
	 * @param verticalAxis The vertical axis;
	 *
	 * @throws AxisNotCompatibleException DOCUMENT-ME
	 */
	public VerticalBarPlot(JFreeChart chart, Axis horizontalAxis, Axis verticalAxis)
					throws AxisNotCompatibleException
	{
		super(chart, horizontalAxis, verticalAxis);
	}

	/**
	 * A convenience method that returns the data source for the plot, cast as a
	 * CategoryDataSource.
	 *
	 * 
	 */
	@Override
  public CategoryDataSource getDataSource() {
		return (CategoryDataSource) chart.getDataSource();
	}

	/**
	 * A convenience method that returns a reference to the vertical axis cast as a
	 * VerticalNumberAxis.
	 *
	 * 
	 */
	public VerticalNumberAxis getValueAxis() {
		return (VerticalNumberAxis) verticalAxis;
	}

	/**
	 * Sets the vertical axis for the plot.  This method should throw an exception if the axis
	 * doesn't implement the required interfaces.
	 *
	 * @param vAxis The new vertical axis.
	 *
	 * @throws AxisNotCompatibleException DOCUMENT-ME
	 */
	@Override
  public void setVerticalAxis(Axis vAxis) throws AxisNotCompatibleException {
		// check that the axis implements the required interface (if not raise an exception);
		super.setVerticalAxis(vAxis);
	}

	/**
	 * A convenience method that returns a reference to the horizontal axis cast as a CategoryAxis.
	 *
	 * 
	 */
	public CategoryAxis getCategoryAxis() {
		return (CategoryAxis) horizontalAxis;
	}

	/**
	 * Sets the horizontal axis for the plot.  This method should throw an exception if the axis
	 * doesn't implement the required interfaces.
	 *
	 * @param axis The new horizontal axis.
	 *
	 * @throws AxisNotCompatibleException DOCUMENT-ME
	 */
	@Override
  public void setHorizontalAxis(Axis axis) throws AxisNotCompatibleException {
		// check that the axis implements the required interface (if not raise an exception);
		super.setHorizontalAxis(axis);
	}

	/**
	 * A convenience method that returns a list of the categories in the data source.
	 *
	 * 
	 */
	@Override
  public java.util.List<?> getCategories() {
		return getDataSource().getCategories();
	}

	/**
	 * Returns the x-coordinate (in Java 2D User Space) of the center of the specified category.
	 *
	 * @param category The index of the category of interest (first category index = 0);
	 * @param area The region within which the plot will be drawn.
	 *
	 * 
	 */
	@Override
  public double getCategoryCoordinate(int category, Rectangle2D area) {
		int seriesCount = getDataSource().getSeriesCount();
		double barWidth = calculateBarWidth(area);

		return area.getX() + introGap +
			   (category * (categoryGap + ((seriesCount - 1) * (seriesGap)) +
			   (seriesCount * barWidth))) +
			   (((seriesCount * (barWidth + seriesGap)) - seriesGap) / 2);
	}

	/**
	 * Returns the width of each bar in the chart.
	 *
	 * @param plotArea The area within which the plot will be drawn.
	 *
	 * 
	 */
	double calculateBarWidth(Rectangle2D plotArea) {
		CategoryDataSource data = getDataSource();

		// series, category and bar counts
		int categoryCount = data.getCategoryCount();
		int seriesCount = data.getSeriesCount();
		int barCount = categoryCount * seriesCount;

		// calculate the plot width (bars are vertical) less whitespace
		double usable = plotArea.getWidth() - introGap - trailGap -
						((categoryCount - 1) * categoryGap) -
						((seriesCount - 1) * categoryCount * seriesGap);

		// and thus the width of the bars
		return usable / barCount;
	}

	/**
	 * Checks the compatibility of a horizontal axis, returning true if the axis is compatible with
	 * the plot, and false otherwise.
	 *
	 * @param axis The horizontal axis;
	 *
	 * 
	 */
	@Override
  public boolean isCompatibleHorizontalAxis(Axis axis) {
		if(axis instanceof CategoryAxis) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks the compatibility of a vertical axis, returning true if the axis is compatible with
	 * the plot, and false otherwise.
	 *
	 * @param axis The vertical axis;
	 *
	 * 
	 */
	@Override
  public boolean isCompatibleVerticalAxis(Axis axis) {
		if(axis instanceof VerticalNumberAxis) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Draws the plot on a Java 2D graphics device (such as the screen or a printer).
	 *
	 * @param g2 The graphics device;
	 * @param drawArea The area within which the plot should be drawn.
	 */
	@Override
  public void draw(Graphics2D g2, Rectangle2D drawArea) {
		if(insets != null) {
			drawArea = new Rectangle2D.Double(drawArea.getX() + insets.left,
											  drawArea.getY() + insets.top,
											  drawArea.getWidth() - insets.left - insets.right,
											  drawArea.getHeight() - insets.top - insets.bottom);
		}

		// we can cast the axes because BarPlot enforces support of these interfaces
		HorizontalAxis ha = getHorizontalAxis();
		VerticalAxis va = getVerticalAxis();

		double h = ha.reserveHeight(g2, this, drawArea);
		Rectangle2D vAxisArea = va.reserveAxisArea(g2, this, drawArea, h);

		// compute the plot area
		Rectangle2D plotArea = new Rectangle2D.Double(drawArea.getX() + vAxisArea.getWidth(),
													  drawArea.getY(),
													  drawArea.getWidth() - vAxisArea.getWidth(),
													  drawArea.getHeight() - h);

		drawOutlineAndBackground(g2, plotArea);

		// draw the axes
		getCategoryAxis().draw(g2, drawArea, plotArea);
		getValueAxis().draw(g2, drawArea, plotArea);

		// empty barAreas
		barAreas.removeAllElements();

		List<Bar> bars = getBars(plotArea); // area should be remaining area only
		Iterator<Bar> iterator = bars.iterator();

		while(iterator.hasNext()) {
			Bar b = iterator.next();
			Rectangle2D barArea = b.getArea();

			// add barArea to barAreas-Vector
			barAreas.add(barArea);

			g2.setPaint(b.getFillPaint());
			g2.fill(barArea);

			if(b.getWidth() > 3) {
				g2.setStroke(b.getOutlineStroke());
				g2.setPaint(b.getOutlinePaint());
				g2.draw(barArea);
			}
		}
	}

	/**
	 * Returns a short string describing the type of plot.
	 *
	 * 
	 */
	@Override
  public String getPlotType() {
		return "Bar Plot";
	}

	/**
	 * Returns a list of bars that will fit inside the specified area.  Note that the list of bars
	 * will be empty if the data source is empty.
	 *
	 * 
	 *
	 * 
	 */
	private List<Bar> getBars(Rectangle2D plotArea) {
		List<Bar> bars = new ArrayList<Bar>();

		// the following cast should be safe...
		CategoryDataSource data = (CategoryDataSource) chart.getDataSource();

		if(data != null) {
			// series, category and bar counts
			int seriesCount = data.getSeriesCount();

			// the width of the bars
			double barWidth = calculateBarWidth(plotArea);

			int currentCategoryIndex = 0;
			Iterator<?> iterator = data.getCategories().iterator();

			while(iterator.hasNext()) {
				Object category = iterator.next();

				for(int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
					// use the series color to fill the bars
					Paint paint = chart.getSeriesPaint(seriesIndex);
					Paint outlinePaint = chart.getSeriesOutlinePaint(seriesIndex);

					// calculate the bar sizes
					Number dataValue = data.getValue(seriesIndex, category);
					double value = dataValue.doubleValue();

					NumberAxis valueAxis = getValueAxis();
					double minimum = valueAxis.getMinimumAxisValue().doubleValue();
					double maximum = valueAxis.getMaximumAxisValue().doubleValue();
					double base = 0.0;
					double top = 0.0;
					boolean barVisible = false;

					if(minimum >= 0.0) {
						if(value > minimum) { // only calculate a bar if the data value exceeds the minimum
							barVisible = true;
							base = valueAxis.translatedValue(valueAxis.getMinimumAxisValue(),
															 plotArea);

							if(value < maximum) {
								top = valueAxis.translatedValue(dataValue, plotArea);
							} else {
								top = valueAxis.translatedValue(valueAxis.getMaximumAxisValue(),
																plotArea);
							}
						}
					} else if(maximum <= 0.0) {
						// only calculate a bar if the data value is less than the maximum
						if(value < maximum) {
							barVisible = true;
							top = valueAxis.translatedValue(valueAxis.getMaximumAxisValue(),
															plotArea);

							if(value > minimum) {
								base = valueAxis.translatedValue(dataValue, plotArea);
							} else {
								base = valueAxis.translatedValue(valueAxis.getMinimumAxisValue(),
																 plotArea);
							}
						}
					} else {
						// only calculate a bar if the data value is non-zero
						if(value > 0.0) {
							barVisible = true;

							if(value < maximum) {
								top = valueAxis.translatedValue(dataValue, plotArea);
								base = valueAxis.translatedValue(Plot.ZERO, plotArea);
							} else {
								top = valueAxis.translatedValue(valueAxis.getMaximumAxisValue(),
																plotArea);
								base = valueAxis.translatedValue(Plot.ZERO, plotArea);
							}
						} else if(value < 0.0) {
							barVisible = true;

							if(value > minimum) {
								top = valueAxis.translatedValue(Plot.ZERO, plotArea);
								base = valueAxis.translatedValue(dataValue, plotArea);
							} else {
								top = valueAxis.translatedValue(Plot.ZERO, plotArea);
								base = valueAxis.translatedValue(valueAxis.getMinimumAxisValue(),
																 plotArea);
							}
						}
					}

					if(barVisible) {
						double barX = plotArea.getX() + introGap +
									  (currentCategoryIndex * (categoryGap +
									  (seriesCount * barWidth))) +
									  (currentCategoryIndex * (seriesCount - 1) * seriesGap) +
									  (seriesIndex * (seriesGap + barWidth));
						bars.add(new Bar(barX, top, barWidth, base - top, new BasicStroke(),
										 outlinePaint, paint));
					}
				}

				currentCategoryIndex++;
			}
		}

		return bars;
	}

	/**
	 * Returns the minimum value in the range, since this is plotted against the vertical axis for
	 * BarPlot.
	 *
	 * 
	 */
	public Number getMinimumVerticalDataValue() {
		DataSource data = this.getChart().getDataSource();

		if(data != null) {
			return DataSources.getMinimumRangeValue(data);
		} else {
			return null;
		}
	}

	/**
	 * Returns the maximum value in either the domain or the range, whichever is displayed against
	 * the vertical axis for the particular type of plot implementing this interface.
	 *
	 * 
	 */
	public Number getMaximumVerticalDataValue() {
		DataSource data = this.getChart().getDataSource();

		if(data != null) {
			return DataSources.getMaximumRangeValue(data);
		} else {
			return null;
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Vector<Rectangle2D> getBarAreas() {
		return barAreas;
	}
}
