package com.intellij.flex.uiDesigner.abc;

import com.intellij.util.ArrayUtil;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.*;

final class Scanner {
  private Scanner() {
  }

  public static int[] scanIntConstants(DataBuffer in) {
    int size = in.readU32();
    if (size == 0) {
      return ArrayUtil.EMPTY_INT_ARRAY;
    }

    int[] positions = new int[size];
    for (int i = 1; i < size; i++) {
      positions[i] = in.position();
      in.readU32();
    }
    return positions;
  }

  public static int[] scanDoubleConstants(DataBuffer in) {
    int size = in.readU32();
    if (size == 0) {
      return ArrayUtil.EMPTY_INT_ARRAY;
    }

    int[] positions = new int[size];
    for (int i = 1; i < size; i++) {
      positions[i] = in.position();
      in.readDouble();
    }
    return positions;
  }

  public static int[] scanStrConstants(DataBuffer in) {
    int size = in.readU32();
    if (size == 0) {
      return ArrayUtil.EMPTY_INT_ARRAY;
    }

    int[] positions = new int[size];
    for (int i = 1; i < size; i++) {
      positions[i] = in.position();
      int length = in.readU32();
      in.skip(length);
    }
    return positions;
  }

  public static int[] scanNsConstants(DataBuffer in) {
    int size = in.readU32();
    if (size == 0) {
      return ArrayUtil.EMPTY_INT_ARRAY;
    }

    int[] positions = new int[size];
    for (int i = 1; i < size; i++) {
      positions[i] = in.position();
      in.skip(1); // kind byte
      in.readU32();
    }
    return positions;
  }

  public static int[] scanNsSetConstants(DataBuffer in) {
    int size = in.readU32();
    if (size == 0) {
      return ArrayUtil.EMPTY_INT_ARRAY;
    }

    int[] positions = new int[size];
    for (int i = 1; i < size; i++) {
      positions[i] = in.position();
      in.skipEntries(in.readU32());
    }
    return positions;
  }

  public static int[] scanMultinameConstants(DataBuffer in) {
    int size = in.readU32();
    if (size == 0) {
      return ArrayUtil.EMPTY_INT_ARRAY;
    }

    int[] positions = new int[size];
    for (int i = 1; i < size; i++) {
      positions[i] = in.position();
      int kind = in.readU8(); // kind byte
      switch (kind) {
        case CONSTANT_Qname:
        case CONSTANT_QnameA:
          in.readU32();
          in.readU32();
          break;
        case CONSTANT_RTQname:
        case CONSTANT_RTQnameA:
          in.readU32();
          break;
        case CONSTANT_Multiname:
        case CONSTANT_MultinameA:
          in.readU32();
          in.readU32();
          break;
        case CONSTANT_RTQnameL:
        case CONSTANT_RTQnameLA:
          break;
        case CONSTANT_MultinameL:
        case CONSTANT_MultinameLA:
          in.readU32();
          break;
        case CONSTANT_TypeName:
          in.readU32(); // name index
          in.skipEntries(in.readU32());
          break;
        default:
          throw new DecoderException("Invalid constant type: " + kind);
      }
    }
    return positions;
  }

  public static int[] scanMethods(DataBuffer in) {
    int size = in.readU32();
    if (size == 0) {
      return ArrayUtil.EMPTY_INT_ARRAY;
    }

    int[] positions = new int[size];

    for (int i = 0; i < size; i++) {
      positions[i] = in.position();

      int paramCount = in.readU32();
      in.readU32(); // ret type
      in.skipEntries(paramCount);
      in.readU32(); //name_index
      int flags = in.readU8();

      long optionalCount = ((flags & METHOD_HasOptional) != 0) ? in.readU32() : 0;
      for (long q = 0; q < optionalCount; ++q) {
        in.readU32();
        in.readU8();
      }
      long paramNameCount = ((flags & METHOD_HasParamNames) != 0) ? paramCount : 0;
      for (long q = 0; q < paramNameCount; ++q) {
        in.readU32();
      }
    }

    return positions;
  }

  public static int[] scanMetadata(DataBuffer in) {
    int size = in.readU32();
    if (size == 0) {
      return ArrayUtil.EMPTY_INT_ARRAY;
    }

    int[] positions = new int[size];

    for (int i = 0; i < size; i++) {
      positions[i] = in.position();
      in.readU32();
      in.skipEntries(in.readU32() * 2);
    }

    return positions;
  }

  public static int[] scanScripts(DataBuffer in) throws DecoderException {
    int size = in.readU32();
    if (size == 0) {
      return ArrayUtil.EMPTY_INT_ARRAY;
    }

    int[] positions = new int[size];

    for (int i = 0; i < size; i++) {
      positions[i] = in.position();
      in.readU32();
      scanTraits(in);
    }

    return positions;
  }

  public static int[] scanMethodBodies(DataBuffer in) throws DecoderException {
    int size = in.readU32();
    if (size == 0) {
      return ArrayUtil.EMPTY_INT_ARRAY;
    }

    int[] positions = new int[size];

    for (int i = 0; i < size; i++) {
      positions[i] = in.position();

      in.skipEntries(5);

      long codeLength = in.readU32();
      in.skip((int)codeLength);

      scanExceptions(in);
      scanTraits(in);
    }

    return positions;
  }

  private static void scanExceptions(DataBuffer in) {
    in.skipEntries(in.readU32() * 5);
  }

  static void scanTraits(DataBuffer in) throws DecoderException {
    int size = in.readU32();
    for (int i = 0; i < size; i++) {
      in.readU32();
      int kind = in.readU8();
      switch (kind & 0x0f) {
        case TRAIT_Var:
        case TRAIT_Const: {
          in.skipEntries(2);
          int valueId = in.readU32();
          if (valueId > 0) {
            in.readU8();
          }
          break;
        }
        case TRAIT_Method:
        case TRAIT_Getter:
        case TRAIT_Setter:
          in.skipEntries(2);
          break;
        case TRAIT_Class:
        case TRAIT_Function:
          in.skipEntries(2);
          break;
        default:
          throw new DecoderException("Invalid trait type: " + kind);
      }

      if (((kind >> 4) & TRAIT_FLAG_metadata) != 0) {
        in.skipEntries(in.readU32());
      }
    }
  }
}
