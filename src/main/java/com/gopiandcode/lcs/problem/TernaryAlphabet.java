package com.gopiandcode.lcs.problem;
/**
 * XCSBinaryClassifier Implementation - Based on An Algorithmic Description of XCSBinaryClassifier - Martin V. Butz and Stewart W.Wilson
 */

public enum TernaryAlphabet {
    ONE, ZERO, HASH;

    public String toString() {
      switch(this) {
          case ONE:
              return "1";
          case ZERO:
              return "0";
          case HASH:
              return "#";
      }
      return null;
    }
}

