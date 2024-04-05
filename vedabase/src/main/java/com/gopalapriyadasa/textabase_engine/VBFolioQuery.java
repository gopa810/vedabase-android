package com.gopalapriyadasa.textabase_engine;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class VBFolioQuery {

	private VBFolioQueryItem currentParseDictionary;
	private ArrayList<ArrayList<VBFolioQueryItem>> currentParseArrayStack;
	private ArrayList<VBFolioQueryItem> currentParseArray;
	private StringBuilder currentParseWord;
	private ArrayList<VBFolioQueryItem> queryParseArray;
	private Folio storage = null;

	public VBFolioQuery(Folio f) {
		storage = f;
	}

	public VBFolioQueryOperator convertQuoteToTree(
			ArrayList<VBFolioQueryItem> array, SearchTreeContext ctx) {
		VBHighlightedPhrase arrTemp = null;
		if (ctx.quotes != null) {
			arrTemp = new VBHighlightedPhrase();
			ctx.quotes.add(arrTemp);

		}

		VBFolioQueryOperatorQuote vq = new VBFolioQueryOperatorQuote();

		for (VBFolioQueryItem d : array) {
			String key = d.getWord();
			// words containing dash character
			// will be splitted into parts
			if (key.indexOf('-') < 0) {
				if (arrTemp != null)
					arrTemp.addWord(key);
				vq.items.add(convertWordToTree(key, ctx));
			} else {
				String[] parts = key.split("-");
				for (String part : parts) {
					if (part.length() > 0) {
						if (arrTemp != null)
							arrTemp.addWord(part);
						vq.items.add(convertWordToTree(part, ctx));
					}
				}
			}
		}

		return vq;
	}

	public VBFolioQueryOperator convertWordToTree(String word,
			SearchTreeContext ctx) {
		if (stringContainsWildcards(word)) {
			ArrayList<String> words = new ArrayList<String>();
			storage.searchWordsForIndex(word, ctx.wordsDomain, words);
			VBFolioQueryOperatorOr vor2 = new VBFolioQueryOperatorOr();
			for (String strWord : words) {
				VBFolioQueryOperatorStream vbs = new VBFolioQueryOperatorStream();
				vbs.setStreams(storage.getWordIndexData(strWord.toLowerCase(),
						ctx.wordsDomain));
				vbs.word = strWord;
				vor2.items.add(vbs);
			}
			return vor2;
		} else {
			VBFolioQueryOperatorStream vbs = new VBFolioQueryOperatorStream();
			vbs.setStreams(storage.getWordIndexData(word.toLowerCase(),
					ctx.wordsDomain));
			vbs.setWord(word);
			return vbs;
		}
	}

	public VBFolioQueryOperator convertItemToTree(VBFolioQueryItem d,
			SearchTreeContext ctx) {
		if (d.getType().equals("meta")) {
			return convertMetaToTree(d.getArray(), ctx);
		} else if (d.getType().equals("string")) {
			return convertQuoteToTree(d.getArray(), ctx);
		} else if (d.getType().equals("array")) {
			return convertArrayToTree(d.getArray(), ctx);
		} else if (d.getType().equals("word")) {
			String word = d.getWord();
			if (word.equals("and"))
				return null;
			return convertWordWithDashesToTree(word, ctx);
		}
		return null;
	}

	public VBFolioQueryOperator convertWordWithDashesToTree(String wordx,
			SearchTreeContext ctx) {
		VBHighlightedPhrase hp = null;
		if (ctx.quotes != null) {
			hp = new VBHighlightedPhrase();
			ctx.quotes.add(hp);
		}
		if (wordx.indexOf('-') < 0) {
			if (hp != null)
				hp.addWord(wordx);
			return convertWordToTree(wordx, ctx);
		} else {
			VBFolioQueryOperatorQuote vq = new VBFolioQueryOperatorQuote();
			String[] parts = wordx.split("-");
			for (String part : parts) {
				if (part.length() > 0) {
					if (hp != null)
						hp.addWord(part);
					vq.items.add(convertWordToTree(part, ctx));
				}
			}
			return vq;
		}
	}

	public VBFolioQueryOperator convertAndQuoteToTree(
			ArrayList<VBFolioQueryItem> array, SearchTreeContext ctx) {
		if (array.size() == 1) {
			return convertItemToTree(array.get(0), ctx);
		} else {
			VBFolioQueryOperatorAnd van = new VBFolioQueryOperatorAnd();
			for (VBFolioQueryItem d : array) {
				VBFolioQueryOperator newOper = convertItemToTree(d, ctx);
				if (newOper != null) {
					van.items.add(newOper);
				}
			}
			return van;
		}
	}

	public VBFolioQueryOperator convertAndNotArrayToTree(
			ArrayList<VBFolioQueryItem> array, SearchTreeContext ctx) {
		boolean hasNot = false;
		for (VBFolioQueryItem d1 : array) {
			if (d1.getType().equals("word") && d1.getWord().equals("not")) {
				hasNot = true;
			}
		}

		if (hasNot == false) {
			return convertAndQuoteToTree(array, ctx);
		} else {
			ArrayList<VBFolioQueryItem> andArray = new ArrayList<VBFolioQueryItem>();
			ArrayList<VBFolioQueryItem> notArray = new ArrayList<VBFolioQueryItem>();

			boolean moveToNot = false;
			for (VBFolioQueryItem d : array) {
				if (d.getType().equals("word") && d.getWord().equals("not")) {
					moveToNot = true;
				} else if (moveToNot == true) {
					notArray.add(d);
					moveToNot = false;
				} else {
					andArray.add(d);
				}
			}

			if (notArray.size() > 0) {
				VBFolioQueryOperatorNot vno = new VBFolioQueryOperatorNot();
				vno.partOr = convertOrQuoteToTree(notArray, ctx);
				vno.partAnd = convertAndQuoteToTree(andArray, ctx);
				return vno;
			} else {
				VBFolioQueryOperator van = convertAndQuoteToTree(andArray, ctx);
				return van;
			}
		}

	}

	public VBFolioQueryOperator convertOrQuoteToTree(
			ArrayList<VBFolioQueryItem> array, SearchTreeContext ctx) {
		VBFolioQueryOperatorOr van = new VBFolioQueryOperatorOr();

		for (VBFolioQueryItem d : array) {
			if (d.getType().equals("string")) {
				van.items.add(convertQuoteToTree(d.getArray(), ctx));
			} else if (d.getType().equals("array")) {
				van.items.add(convertArrayToTree(d.getArray(), ctx));
			} else if (d.getType().equals("word")) {
				String word = d.getWord();
				van.items.add(convertWordWithDashesToTree(word, ctx));
			}
		}

		return van;
	}

	public VBFolioQueryOperator convertArrayToTree(
			ArrayList<VBFolioQueryItem> array, SearchTreeContext ctx) {
		if (array == null)
			return null;

		boolean hasOr = false;
		for (VBFolioQueryItem d1 : array) {
			if (d1.getType().equals("word")) {
				if (d1.getWord().equals("or")) {
					hasOr = true;
					break;
				}
			}
		}

		if (hasOr == false) {
			return convertAndNotArrayToTree(array, ctx);
		} else {
			ArrayList<ArrayList<VBFolioQueryItem>> arr1 = new ArrayList<ArrayList<VBFolioQueryItem>>();
			ArrayList<VBFolioQueryItem> arr2 = new ArrayList<VBFolioQueryItem>();
			arr1.add(arr2);

			for (VBFolioQueryItem d1 : array) {
				if (d1.getType().equals("word") && d1.getWord().equals("or")) {
					arr2 = new ArrayList<VBFolioQueryItem>();
					arr1.add(arr2);
				} else {
					arr2.add(d1);
				}
			}

			VBFolioQueryOperatorOr vor = new VBFolioQueryOperatorOr();
			for (ArrayList<VBFolioQueryItem> arr3 : arr1) {
				if (arr3.size() > 0) {
					VBFolioQueryOperator itemNew = convertAndNotArrayToTree(
							arr3, ctx);
					if (itemNew != null)
						vor.items.add(itemNew);
				}
			}

			return vor;
		}
	}

	//
	// converting scope tags into tree
	// [Headings ...]
	// [Contents ...]
	// [Level ......]
	// [Group ......]
	// [Field ......]
	// [Note .......]
	// [Popup ......]
	//

	public VBFolioQueryOperator convertMetaToTree(
			ArrayList<VBFolioQueryItem> array, SearchTreeContext ctx) {
		VBFolioQueryItem item = null;
		if (array.size() == 0)
			return null;

		item = array.get(0);
		if (item.getType().equals("word")) {
			//
			// [Note ....]
			//
			if (item.getWord().equals("note")) {

				return convertMetaNote(array, ctx);
			}
			//
			// Popup .....
			//
			else if (item.getWord().equals("popup")) {

				return convertMetaPopup(array, ctx);
			}
			//
			// Group .....
			//
			else if (item.getWord().equals("group")) {

				return convertMetaGroup(array);
			}
			//
			// Field N : .....
			//
			else if (item.getWord().equals("field")) {

				return convertMetaField(array, ctx);
			}
			//
			// Level N : .....
			//
			else if (item.getWord().equals("level")) {

				return convertMetaLevel(array, ctx);
			}
			//
			// Headings X, N1, N2, ... NN
			//
			else if (item.getWord().equals("headings")) {

				return convertMetaHeadings(array, ctx);

			}
			//
			// Contents N1, N2, .... NN
			//
			else if (item.getWord().equals("contents")) {

				return convertMetaContents(array);
			}
		}

		return null;
	}

	public VBFolioQueryOperator convertMetaNote(
			ArrayList<VBFolioQueryItem> array, SearchTreeContext ctx) {
		ArrayList<VBFolioQueryItem> args = subArrayWithoutFirst(array);

		SearchTreeContext ctx2 = new SearchTreeContext();
		ctx2.quotes = ctx.quotes;
		ctx2.wordsDomain = "Note";
		ctx2.exactWords = ctx.exactWords;

		if (args.size() > 0) {
			return convertArrayToTree(args, ctx2);
		} else {
			return convertWordToTree("<all>", ctx2);
		}
	}

	public VBFolioQueryOperator convertMetaPopup(
			ArrayList<VBFolioQueryItem> array, SearchTreeContext ctx) {
		ArrayList<VBFolioQueryItem> args = subArrayWithoutFirst(array);

		SearchTreeContext ctx2 = new SearchTreeContext();
		ctx2.quotes = ctx.quotes;
		ctx2.wordsDomain = "Popup";
		ctx2.exactWords = ctx.exactWords;

		if (args.size() > 0) {
			return convertArrayToTree(args, ctx2);
		} else {
			return convertWordToTree("<all>", ctx2);
		}
	}

	public VBFolioQueryOperator convertMetaGroup(
			ArrayList<VBFolioQueryItem> array) {
		StringBuilder groupName = new StringBuilder();
		int i = 1;
		while (array.size() > i) {
			VBFolioQueryItem d = array.get(i);
			if (d.getType().equals("word")) {
				if (groupName.length() > 0)
					groupName.append(" ");
				groupName.append(d.getOriginalWord());
			}
			i++;
		}
		VBFolioQueryOperatorRecords grec = new VBFolioQueryOperatorRecords();
		ArrayList<Integer> records = new ArrayList<Integer>();
		storage.enumerateGroupRecords(groupName.toString(), records);
		grec.AddArray(records);

		VBFolioQueryOperatorGetSubRanges subs = new VBFolioQueryOperatorGetSubRanges();
		subs.storage = storage;
		subs.source = grec;

		return subs;
	}

	public VBFolioQueryOperator convertMetaField(
			ArrayList<VBFolioQueryItem> array, SearchTreeContext ctx) {
		StringBuilder fieldName = new StringBuilder();
		ArrayList<VBFolioQueryItem> args = new ArrayList<VBFolioQueryItem>();

		int i = 1;
		while (array.size() > i) {
			VBFolioQueryItem d = array.get(i);
			if (d.getType().equals("meta:dots")) {
				i++;
				break;
			}
			if (d.getType().equals("word")) {
				if (fieldName.length() > 0)
					fieldName.append(" ");
				fieldName.append(d.getOriginalWord());
			}
			i++;
		}
		while (array.size() > i) {
			args.add(array.get(i));
			i++;
		}
		VBFolioQueryOperator prevOper = null;
		if (fieldName != null && fieldName.length() > 0) {
			if (args.size() > 0) {
				SearchTreeContext ctx2 = new SearchTreeContext();
				ctx2.quotes = ctx.quotes;
				ctx2.wordsDomain = fieldName.toString();
				ctx2.exactWords = ctx.exactWords;

				prevOper = convertArrayToTree(args, ctx2);
			} else {
				SearchTreeContext ctx2 = new SearchTreeContext();
				ctx2.quotes = ctx.quotes;
				ctx2.wordsDomain = fieldName.toString();
				ctx2.exactWords = ctx.exactWords;

				prevOper = convertWordToTree("<all>", ctx2);
			}
		} else {
			SearchTreeContext ctx2 = new SearchTreeContext();
			ctx2.quotes = ctx.quotes;
			ctx2.wordsDomain = "<all>";
			ctx2.exactWords = ctx.exactWords;

			prevOper = convertWordToTree("<all>", ctx2);
		}

		return prevOper;
	}

	public VBFolioQueryOperator convertMetaLevel(
			ArrayList<VBFolioQueryItem> array, SearchTreeContext ctx) {
		StringBuilder levelName = new StringBuilder();
		ArrayList<VBFolioQueryItem> args = new ArrayList<VBFolioQueryItem>();

		int i = 1;
		while (array.size() > i) {
			VBFolioQueryItem d = array.get(i);
			if (d.getType().equals("meta:dots")) {
				i++;
				break;
			}
			if (d.getType().equals("word")) {
				if (levelName.length() > 0)
					levelName.append(" ");
				levelName.append(d.getOriginalWord());
			}
			i++;
		}
		while (array.size() > i) {
			args.add(array.get(i));
			i++;
		}
		int level = storage.findOriginalLevelIndex(levelName.toString());

		VBFolioQueryOperatorGetLevelRecords levelOper = new VBFolioQueryOperatorGetLevelRecords(
				storage);
		levelOper.levelIndex = level;
		levelOper.exactWords = ctx.exactWords;
		VBFolioQueryOperator prevOper = levelOper;

		if (args.size() > 0) {
			VBFolioQueryOperatorGetSubRanges subs = new VBFolioQueryOperatorGetSubRanges();

			subs.storage = storage;
			subs.source = prevOper;

			VBFolioQueryOperator oper = convertArrayToTree(args, ctx);

			VBFolioQueryOperatorAnd ands = new VBFolioQueryOperatorAnd();
			ands.items.add(subs);
			ands.items.add(oper);

			prevOper = ands;
		}

		return prevOper;
	}

	public VBFolioQueryOperator convertMetaHeadings(
			ArrayList<VBFolioQueryItem> array, SearchTreeContext ctx) {
		ArrayList<StringBuilder> args = new ArrayList<StringBuilder>();
		StringBuilder str1 = new StringBuilder();
		args.add(str1);

		int i = 1;
		while (array.size() > i) {
			VBFolioQueryItem d = array.get(i);
			if (d.getType().equals("meta:comma")) {
				str1 = new StringBuilder();
				args.add(str1);
			}
			if (d.getType().equals("word")) {
				if (str1.length() > 0)
					str1.append(" ");
				str1.append(d.getOriginalWord());
			}
			i++;
		}
		int level = storage.findOriginalLevelIndex(args.get(0).toString());

		VBFolioQueryOperatorGetLevelRecords levelOper = new VBFolioQueryOperatorGetLevelRecords(
				storage);
		levelOper.levelIndex = level;
		levelOper.exactWords = ctx.exactWords;
		VBFolioQueryOperator prevOper = levelOper;

		for (int subi = 1; subi < args.size(); subi++) {
			if (subi == 1) {
				levelOper.simpleTitle = args.get(subi).toString().toLowerCase()
						.replace(". ", " ");
			} else if (subi > 1) {
				VBFolioQueryOperatorContentSubItems subIt1 = new VBFolioQueryOperatorContentSubItems();
				subIt1.storage = storage;
				subIt1.source = prevOper;
				prevOper = subIt1;

				VBFolioQueryOperatorContentItems cit = new VBFolioQueryOperatorContentItems();
				cit.storage = storage;
				cit.simpleText = args.get(subi).toString().toLowerCase()
						.replace(". ", " ");
				cit.exactWords = ctx.exactWords;

				VBFolioQueryOperatorAnd join1 = new VBFolioQueryOperatorAnd();
				join1.items.add(cit);
				join1.items.add(prevOper);
				prevOper = join1;
			}
		}

		VBFolioQueryOperatorGetSubRanges subOper = new VBFolioQueryOperatorGetSubRanges();
		subOper.storage = storage;
		subOper.source = prevOper;

		return subOper;
	}

	public VBFolioQueryOperator convertMetaContents(
			ArrayList<VBFolioQueryItem> array) {
		ArrayList<StringBuilder> args = new ArrayList<StringBuilder>();
		StringBuilder str1 = new StringBuilder();
		args.add(str1);

		int i = 1;
		while (array.size() > i) {
			VBFolioQueryItem d = array.get(i);
			if (d.getType().equals("meta:comma")) {
				str1 = new StringBuilder();
				args.add(str1);
			}
			if (d.getType().equals("word")) {
				if (str1.length() > 0)
					str1.append(" ");
				str1.append(d.getWord());
			}
			i++;
		}

		VBFolioQueryOperatorRecords recsOper = new VBFolioQueryOperatorRecords();
		recsOper.add(0);

		VBFolioQueryOperator prevOper = recsOper;

		for (int subi = 0; subi < args.size(); subi++) {
			VBFolioQueryOperatorContentSubItems subIt1 = new VBFolioQueryOperatorContentSubItems();
			subIt1.storage = storage;
			subIt1.source = prevOper;
			prevOper = subIt1;

			VBFolioQueryOperatorContentItems cit = new VBFolioQueryOperatorContentItems();
			cit.storage = storage;
			cit.simpleText = args.get(subi).toString();

			VBFolioQueryOperatorAnd join1 = new VBFolioQueryOperatorAnd();
			join1.items.add(cit);
			join1.items.add(prevOper);
			prevOper = join1;

		}

		VBFolioQueryOperatorGetSubRanges subOper = new VBFolioQueryOperatorGetSubRanges();
		subOper.storage = storage;
		subOper.source = prevOper;

		return subOper;
	}

	public ArrayList<VBFolioQueryItem> subArrayWithoutFirst(
			ArrayList<VBFolioQueryItem> array) {
		ArrayList<VBFolioQueryItem> args;
		args = new ArrayList<VBFolioQueryItem>();
		args.addAll(array);
		args.remove(0);
		return args;
	}

	public ArrayList<VBFolioQueryItem> sourceToArray(String text) {

		byte[] query = null;
		try {
			query = text.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		acceptOtherCharacter();
		currentParseArray = null;
		currentParseArrayStack = new ArrayList<ArrayList<VBFolioQueryItem>>();
		currentParseDictionary = null;
		queryParseArray = null;

		boolean inQuote = false;

		int len = query.length;

		for (int a = 0; a < len; a++) {
			byte c = query[a];
			if (Character.isLetterOrDigit(c) || c == '%' || c == '*'
					|| c == '?' || c == '\'' || c == '-' || c == '.') {
				acceptWordCharacter(c);
			} else if (c == '\"' || c == '\'') {
				inQuote = acceptQuoteCharacter(inQuote);
			} else if (c == '(' || c == '[') {
				acceptOpenBracketCharacter(c);
			} else if (c == ')' || c == ']') {
				acceptCloseBracketCharacter();
			} else if (c == '&') {
				acceptAndOperator();
			} else if (c == '|' || c == '/') {
				acceptOrOperator();
			} else if (c == ',') {
				acceptColonCharacter();
			} else if (c == ':') {
				acceptDoubleDotCharacter();
			} else {
				// other characters are just separators
				acceptOtherCharacter();
			}
		}

		return queryParseArray;

	}

	public void acceptOtherCharacter() {
		currentParseWord = null;
	}

	public void acceptDoubleDotCharacter() {
		currentParseArray.add(new VBFolioQueryItem("meta:dots", null));
		acceptOtherCharacter();
	}

	public void acceptColonCharacter() {
		currentParseArray.add(new VBFolioQueryItem("meta:comma", null));
		acceptOtherCharacter();
	}

	public void acceptOrOperator() {
		initQueryParseArray();
		currentParseArray.add(new VBFolioQueryItem("word", "or"));
		acceptOtherCharacter();
	}

	public void acceptAndOperator() {
		initQueryParseArray();

		acceptOtherCharacter();
	}

	public void acceptCloseBracketCharacter() {
		initQueryParseArray();
		if (currentParseArrayStack.size() > 1) {
			currentParseArrayStack.remove(currentParseArrayStack.size() - 1);
			currentParseArray = currentParseArrayStack
					.get(currentParseArrayStack.size() - 1);
			currentParseDictionary = currentParseArray.get(currentParseArray
					.size() - 1);
		}
		acceptOtherCharacter();
	}

	public void acceptOpenBracketCharacter(byte c) {
		initQueryParseArray();
		currentParseDictionary = new VBFolioQueryItem();
		currentParseArray.add(currentParseDictionary);
		currentParseDictionary.setType(c == '(' ? "array" : "meta");

		currentParseArray = new ArrayList<VBFolioQueryItem>();
		currentParseDictionary.setArray(currentParseArray);
		currentParseArrayStack.add(currentParseArray);
		acceptOtherCharacter();
	}

	public boolean acceptQuoteCharacter(boolean inQuote) {
		initQueryParseArray();

		if (inQuote) {
			if (currentParseArrayStack.size() > 1) {
				currentParseArrayStack
						.remove(currentParseArrayStack.size() - 1);
				currentParseArray = currentParseArrayStack
						.get(currentParseArrayStack.size() - 1);
				currentParseDictionary = currentParseArray
						.get(currentParseArray.size() - 1);
			}
			inQuote = false;
		} else {
			currentParseDictionary = new VBFolioQueryItem();
			currentParseArray.add(currentParseDictionary);

			currentParseDictionary.setType("string");
			currentParseArray = new ArrayList<VBFolioQueryItem>();
			currentParseDictionary.setArray(currentParseArray);
			currentParseArrayStack.add(currentParseArray);
			inQuote = true;
		}
		acceptOtherCharacter();
		return inQuote;
	}

	public void acceptWordCharacter(byte c) {

		initQueryParseArray();

		if (currentParseWord == null) {
			currentParseDictionary = new VBFolioQueryItem();
			currentParseArray.add(currentParseDictionary);

			currentParseWord = new StringBuilder();
			currentParseDictionary.setWordBuilder(currentParseWord);
			currentParseDictionary.setType("word");
		}

		if (c == '*')
			c = '%';
		currentParseWord.append((char)c);
	}

	public void initQueryParseArray() {
		if (queryParseArray == null) {
			queryParseArray = new ArrayList<VBFolioQueryItem>();
			currentParseArrayStack.add(queryParseArray);
			currentParseArray = queryParseArray;
		}
	}

	public boolean stringContainsWildcards(String word) {

		return (word.indexOf('%') >= 0 || word.indexOf('?') >= 0 || word
				.indexOf('*') >= 0);
	}
}
