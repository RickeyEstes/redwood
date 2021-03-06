/* 
 * Redwood -- a framework for logging in Java
 * Copyright (c) 2012 The Board of Trustees of
 * The Leland Stanford Junior University. All Rights Reserved.
 *
 * Redwood is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 * 
 * Redwood is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package edu.stanford.nlp.util.logging;

import edu.stanford.nlp.util.logging.Redwood.Record;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A filter for selecting which channels are visible. This class
 * behaves as an "or" filter; that is, if any of the filters are considered
 * valid, it allows the Record to proceed to the next handler.
 *
 * @author Gabor Angeli (angeli at cs.stanford)
 */
public class VisibilityHandler extends LogRecordHandler {
  private static enum State { SHOW_ALL, HIDE_ALL }

  private VisibilityHandler.State defaultState = State.SHOW_ALL;
  private final HashSet<Object> deltaPool = new HashSet<Object>();

  /**
   * Show all of the channels.
   */
  public void showAll() {
    this.defaultState = State.SHOW_ALL;
    this.deltaPool.clear();
  }

  /**
   * Show none of the channels
   */
  public void hideAll() {
    this.defaultState = State.HIDE_ALL;
    this.deltaPool.clear();
  }

  /**
   * Show all the channels currently being printed, in addition
   * to a new one
   * @param filter The channel to also show
   * @return true if this channel was already being shown.
   */
  public boolean alsoShow(Object filter){
    switch(this.defaultState){
    case HIDE_ALL:
      return this.deltaPool.add(filter);
    case SHOW_ALL:
      return this.deltaPool.remove(filter);
    default:
      throw new IllegalStateException("Unknown default state setting: " + this.defaultState);
    }
  }

  /**
   * Show all the channels currently being printed, with the exception
   * of this new one
   * @param filter The channel to also hide
   * @return true if this channel was already being hidden.
   */
  public boolean alsoHide(Object filter){
    switch(this.defaultState){
    case HIDE_ALL:
      return this.deltaPool.remove(filter);
    case SHOW_ALL:
      return this.deltaPool.add(filter);
    default:
      throw new IllegalStateException("Unknown default state setting: " + this.defaultState);
    }
  }

  /** {@inheritDoc} */
  public List<Record> handle(Record record) {
    boolean isPrinting = false;
    if(record.force()){
      //--Case: Force Printing
      isPrinting = true;
    } else {
      //--Case: Filter
      switch (this.defaultState){
        case HIDE_ALL:
          //--Default False
          for(Object tag : record.channels()){
            if(this.deltaPool.contains(tag)){
              isPrinting = true;
              break;
            }
          }
          break;
        case SHOW_ALL:
          //--Default True
          boolean somethingSeen =  false;
          for(Object tag : record.channels()){
            if(this.deltaPool.contains(tag)){ somethingSeen = true; break; }
          }
          isPrinting = !somethingSeen;
          break;
        default:
          throw new IllegalStateException("Unknown default state setting: " + this.defaultState);
      }
    }
    //--Return
    if(isPrinting){
      ArrayList<Record> retVal = new ArrayList<Record>();
      retVal.add(record);
      return retVal;
    } else {
      return EMPTY;
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<Record> signalStartTrack(Record signal) {
    return EMPTY;
  }
  /** {@inheritDoc} */
  @Override
  public List<Record> signalEndTrack(int newDepth, long timeOfEnd) {
    return EMPTY;
  }
}
