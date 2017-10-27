package es.inteco.common;

public interface CheckFunctionConstants {

    int CODE_TYPE_NOTSET = 0;
    int CODE_TYPE_CONDITION = 1;
    int CODE_TYPE_FUNCTION = 2;
    int CODE_TYPE_LANGUAGE = 3;

    int CONDITION_NOTSET = 0;
    int CONDITION_AND = 1;
    int CONDITION_OR = 2;

    int FUNCTION_NOTSET = 0;
    int FUNCTION_TEXT_EQUALS = 1;
    int FUNCTION_TEXT_NOTEQUALS = 2;
    int FUNCTION_ATTRIBUTE_EXISTS = 3;
    int FUNCTION_ATTRIBUTE_MISSING = 4;
    int FUNCTION_ATTRIBUTES_SAME = 5;
    int FUNCTION_ATTRIBUTE_NOTUNIQUE = 6;
    int FUNCTION_ELEMENT_COUNT_GREATER = 7;
    int FUNCTION_ELEMENT_COUNT_LESS = 8;
    int FUNCTION_ELEMENT_COUNT_EQUALS = 9;
    int FUNCTION_ELEMENT_COUNT_NOTEQUALS = 10;
    int FUNCTION_ATTRIBUTE_NULL = 11;
    int FUNCTION_CHARS_GREATER = 12;
    int FUNCTION_CHARS_LESS = 13;
    int FUNCTION_NUMBER_ANY = 14;
    int FUNCTION_NUMBER_LESS_THAN = 15;
    int FUNCTION_NUMBER_GREATER_THAN = 16;
    int FUNCTION_CONTAINER = 17;
    int FUNCTION_NOTCONTAINER = 18;
    int FUNCTION_TEXT_LINK_EQUIVS_MISSING = 19;
    int FUNCTION_LABEL_NOT_ASSOCIATED = 20;
    int FUNCTION_LABEL_NO_TEXT = 21;
    int FUNCTION_DLINK_MISSING = 22;
    int FUNCTION_NEXT_HEADING_BAD = 23;
    int FUNCTION_PREV_HEADING_BAD = 24;
    int FUNCTION_NOSCRIPT_MISSING = 26;
    int FUNCTION_NOEMBED_MISSING = 27;
    int FUNCTION_ALTLINKSAME = 28;
    int FUNCTION_ROW_COUNT = 29;
    int FUNCTION_COL_COUNT = 30;
    int FUNCTION_ELEMENT_PREVIOUS = 31;
    int FUNCTION_TARGETS_SAME = 32;
    int FUNCTION_HTML_CONTENT_NOT = 33;
    int FUNCTION_HAS_LANGUAGE = 34;
    int FUNCTION_NOT_VALID_LANGUAGE = 35;
    int FUNCTION_MULTIRADIO_NOFIELDSET = 36;
    int FUNCTION_MULTICHECKBOX_NOFIELDSET = 37;
    int FUNCTION_LUMINOSITY_CONTRAST_RATIO = 38;
    int FUNCTION_ERT_COLOR_ALGORITHM = 39;
    int FUNCTION_DOCTYPE_ATTRIBUTE_NOT_EQUAL = 40;
    int FUNCTION_VALIDATE = 41;
    int FUNCTION_TABLE_TYPE = 43;
    int FUNCTION_MISSING_ID_HEADERS = 44;
    int FUNCTION_MISSING_SCOPE = 45;
    int FUNCTION_CAPTION_SUMMARY_SAME = 46;
    int FUNCTION_LABEL_NOT_CLOSE = 48;
    int FUNCTION_SUBMIT_LABELS_DIFFERENT = 49;
    int FUNCTION_IS_ONLY_BLANKS = 50;
    int FUNCTION_NOT_VALID_URL = 51;
    int FUNCTION_OBJECT_HAS_NOT_ALTERNATIVE = 52;
    int FUNCTION_ATTRIBUTES_NOT_SAME = 53;
    int FUNCTION_NOFRAME_MISSING = 54;
    int FUNCTION_IFRAME_HAS_NOT_ALTERNATIVE = 55;
    int FUNCTION_GRAMMAR_LANG = 56;
    int FUNCTION_INCORRECT_HEADING_STRUCTURE = 57;
    int FUNCTION_NO_CORRECT_DOCUMENT_STRUCTURE = 58;
    int FUNCTION_HEADERS_MISSING = 59;
    int FUNCTION_HEADERS_EXIST = 60;
    int FUNCTION_TABLE_HEADING_COMPLEX = 61;
    int FUNCTION_HAS_ALL_ID_HEADERS = 62;
    int FUNCTION_CONTAINS = 63;
    int FUNCTION_CONTAINS_NOT = 64;
    int FUNCTION_CHECK_COLORS = 65;
    int FUNCTION_ALL_ELEMENTS_NOT_LIKE_THIS = 66;
    int FUNCTION_DEFINITION_LIST_CONSTRUCTION = 67;
    int FUNCTION_NOT_VALID_DOCTYPE = 68;
    int FUNCTION_HAS_ELEMENT_INTO = 69;
    int FUNCTION_SAME_FOLLOWING_LIST = 70;
    int FUNCTION_SAME_FOLLOWING_LIST_NOT = 71;
    int FUNCTION_TEXT_CONTAIN_GENERAL_QUOTE = 74;
    int FUNCTION_SAME_ELEMENT_NOT = 75;
    int FUNCTION_SAME_ELEMENT = 76;
    int FUNCTION_HAS_NBSP_ENTITIES = 77;
    int FUNCTION_NOT_IS_ONLY_BLANKS = 78;
    int FUNCTION_LINK_SAME_PAGE = 79;
    int FUNCTION_METADATA_MISSING = 80;
    int FUNCTION_NOT_ALL_LABELS = 81;
    int FUNCTION_LABEL_INCORRECTLY_ASSOCIATED = 82;
    int FUNCTION_TEXT_MATCH = 83;
    int FUNCTION_TEXT_NOT_MATCH = 84;
    int FUNCTION_IS_ODD = 85;
    int FUNCTION_IS_EVEN = 86;
    int FUNCTION_VALIDATE_CSS = 87;
    int FUNCTION_HAS_NOT_ELEMENT_CHILDS = 88;
    int FUNCTION_USER_DATA_MATCHS = 89;
    int FUNCTION_NOT_USER_DATA_MATCHS = 90;
    int FUNCTION_IS_NOT_ONLY_BLANKS = 91;
    int FUNCTION_NOT_CHILDREN_HAVE_ATTRIBUTE = 92;
    int FUNCTION_NUM_MORE_CONTROLS = 93;
    int FUNCTION_NOT_CLEAR_LANGUAGE = 94;
    int FUNCTION_HAS_NOT_SECTION_LINK = 95;
    int FUNCTION_NOT_CORRECT_HEADING = 96;
    int FUNCTION_FALSE_PARAGRAPH_LIST = 97;
    int FUNCTION_FALSE_BR_LIST = 98;
    int FUNCTION_FALSE_HEADER_WITH_ONLY_CELL = 99;
    int FUNCTION_IS_EMPTY_ELEMENT = 100;
    int FUNCTION_HAS_INCORRECT_TABINDEX = 101;
    int FUNCTION_DUPLICATE_FOLLOWING_HEADERS = 102;
    int FUNCTION_ATTRIBUTE_ELEMENT_TEXT_MATCH = 103;
    int FUNCTION_HAS_NOT_ENOUGH_TEXT = 104;
    int FUNCTION_LANGUAGE_NOT_EQUALS = 105;
    int FUNCTION_EMPTY_ELEMENTS = 106;
    int FUNCTION_ELEMENTS_EXCESSIVE_USAGE = 107;
    int FUNCTION_ATTRIBUTES_EXCESSIVE_USAGE = 108;
    int FUNCTION_ELEMENT_PERCENTAGE = 109;
    int FUNCTION_CORRECT_LINKS = 110;
    int FUNCTION_CHILD_ELEMENT_CHARS_GREATER = 111;
    int FUNCTION_CHILD_ELEMENT_CHARS_LESSER = 136;
    int FUNCTION_LAYOUT_TABLE = 112;
    int FUNCTION_LAYOUT_TABLE_NUMBER = 113;
    int FUNCTION_NOT_LAYOUT_TABLE = 114;
    int FUNCTION_INTERNAL_ELEMENT_COUNT_GREATER = 115;
    int FUNCTION_NOT_EXTERNAL_URL = 116;
    int FUNCTION_ACCESSIBILITY_DECLARATION_NO_CONTACT = 117;
    int FUNCTION_ACCESSIBILITY_DECLARATION_NO_REVISION_DATE = 118;
    int FUNCTION_HAS_COMPLEX_STRUCTURE = 119;
    int FUNCTION_TOO_MANY_BROKEN_LINKS = 120;
    int FUNCTION_APPLET_HAS_NOT_ALTERNATIVE = 121;
    int FUNCTION_EXIST_ATTRIBUTE_VALUE = 122;
    int FUNCTION_IFRAME_HAS_ALTERNATIVE = 123;
    int FUNCTION_NOT_EXIST_ATTRIBUTE_VALUE = 124;
    int FUNCTION_EMPTY_SECTION = 125;
    int FUNCTION_COUNT_ATTRIBUTE_VALUE_GREATER_THAN = 126;
    int FUNCTION_CHILD_CONTAIN = 127;
    int FUNCTION_IS_ANIMATED_GIF = 128;
    int FUNCTION_APPLET_HAS_ALTERNATIVE = 129;
    int FUNCTION_ATTRIBUTE_ELEMENT_TEXT_NOT_MATCH = 130;
    int FUNCTION_FOLLOWING_HEADERS_WITHOUT_CONTENT = 131;
    int FUNCTION_IMG_DIMENSIONS_LESS_THAN = 132;
    int FUNCTION_REDUNDANT_IMG_ALT = 133;
    int FUNCTION_TABLE_COLUMNS = 134;
    int FUNCTION_TABLE_ROWS = 135;
    int FUNCTION_HAS_VALIDATION_ERRORS = 137;
    int FUNCTION_GUESS_LANGUAGE = 138;
    int FUNCTION_GROUPED_SELECTION_BUTTONS = 139;
    int FUNCTION_NOT_FIRST_CHILD = 140;
    int FUNCTION_REQUIRED_CONTROLS = 141;
    int FUNCTION_CSS_GENERATED_CONTENT = 142;
    int FUNCTION_CSS_COLOR_CONTRAST = 143;
    int FUNCTION_CSS_BLINK = 144;
    int FUNCTION_CSS_PARSEABLE = 145;
    int FUNCTION_CSS_OUTLINE = 146;
    int FUNCTION_FALSE_BR_IMAGE_LIST = 147;
    int FUNCTION_OTHER_LANGUAGE = 148;
    int FUNCTION_CSS_LABEL_HIDDEN = 149;
    int FUNCTION_CURRENT_LANGUAGE = 150;
    int FUNCTION_INVALID_SCOPE = 151;
    int FUNCTION_ACCESSIBILITY_DECLARATION_NO_CONFORMANCE_LEVEL = 152;
    int FUNCTION_TABINDEX_EXCESSIVE_USAGE = 153;
    int FUNCTION_LINK_CHARS_GREATER = 154;
    int FUNCTION_TABLE_HEADING_BLANK = 155;
    int FUNCTION_TITLE_NOT_CONTAINS = 156;
    int FUNCTION_TABLE_COMPLEX = 157;
    
    //TODO 2017 Nuevos códigos de funciones
    int FUNCTION_ARIA_LABELLEDBY_REFERENCED = 158;
    int FUNCTION_ATTRIBUTE_LENGHT = 159;
    int FUNCTION_ATTRIBUTE_LABELEDBY_LENGHT = 160;
    int FUNCTION_HEADERS_WAI_MISSING = 161;
    int FUNCTION_HEADERS_WAI_LEVEL_1_MISSING = 162;
    int FUNCTION_FOLLOWING_WAI_HEADERS_WITHOUT_CONTENT = 170;
    int FUNCTION_SKIP_WAI_HEADERS_LEVEL= 171;
    int FUNCTION_ELEMENT_COUNT_ATTRIBUTE_VALUE = 172;
    int FUNCTION_ACCESSIBILITY_CONTACT_FORM = 173;

    // check codes
    int CHECK_STATUS_OK = 1;
    int CHECK_STATUS_PREREQUISITE_NOT_PRINT = 2;
    int CHECK_STATUS_UNINITIALIZED = 0;
    int CHECK_STATUS_BAD_FUNCTION = -1;
    int CHECK_STATUS_BAD_PARAMS = -2;
    int CHECK_STATUS_BAD_TRIGGER = -3;
    int CHECK_STATUS_BAD_FUNCTION_INIT = -4;
    int CHECK_STATUS_BAD_ALGORITHM = -5;
    int CHECK_STATUS_BAD_TEST = -6;
    int CHECK_STATUS_BAD_ID = -7;

    int CONFIDENCE_NOT_SET = 0;
    int CONFIDENCE_LOW = 1;
    int CONFIDENCE_MEDIUM = 2;
    int CONFIDENCE_HIGH = 3;
    int CONFIDENCE_CANNOTTELL = 4;

    int CODE_RESULT_IGNORE = 0;
    int CODE_RESULT_PROBLEM = 1;
    int CODE_RESULT_NOPROBLEM = 2;

    int COMPARE_EQUAL = 0;
    int COMPARE_LESS_THAN = 1;
    int COMPARE_GREATER_THAN = 2;
	
	
}
