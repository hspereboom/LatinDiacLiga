/*
 * MIT License
 *
 * Copyright (C) 2013-2025 Harry Shungo Pereboom (github.com/hspereboom)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package LDL;

import javax.annotation.processing.Generated;

/**
 * Replaces latin diacritics and ligatures in the given string with plain chars.
 *
 * <pre>
 * [About]
 *
 * DUCET-4 NFD (Canonical Decomposition) doesn't reduce each diacritic to its
 * base character alone, but injects <u>modifier graphemes</u>, making it
 * unsuitable for comparison against 'lazy' IA5 strings.
 *
 * Example:
 *   Suppose we have some text containing the sequence "açe", which we want to
 *   be able to match using the (pragmatic) term "ace".
 *
 *   While {@link String#indexOf(String)} is too strict by default for our
 *   purpose, {@link java.text.Normalizer#normalize(String)} will produce a
 *   NFD of "açe", [ac ◦ U+0327 ('COMBINING CEDILLA') ◦ e], which also does
 *   not suffice.
 *
 * Instead, we employ a custom locale-invariant translation that omits diacritic
 * modifiers entirely and expands the ligatures. *1
 *
 * Example:
 *   Because [ö] is not a ligature, it becomes [o], not [oe];
 *   Because [å] is not a ligature, it becomes [a], not [aa];
 *   Because [ß] is not a diacritic, it becomes [ss], not [s] or [B].
 * </pre>
 *
 * <pre>
 * [Coverage]
 *
 * Unicode 17.0 (August 2025)
 * </pre>
 *
 * <pre>
 * [Caveats]
 *
 * <u>Phonetics</u>
 *
 * Whereas normalization of ASCII-based composites (diacritics & ligatures)
 * is trivial, many UCD glyphs represent spoken sounds. These are usually
 * non-composites, some of which were ported from Latin glyphs and altered
 * in appearance. Instead of attempting to express the sounds in ASCII form,
 * we shall replace them with their original glyphs.
 *
 * Example:
 *     U+0023A represents the Saanich sound 'ej', and has a non-composite
 *     glyph ported from the Latin 'A'. Therefore, we use 'A' as its normal.
 *
 * Example:
 *     U+00138 represents the Kalaallisut sound 'q', and has a non-composite
 *     glyph ported from the Latin 'K'. We use 'K' as its normal, despite
 *     the glyph having been replaced by Q.
 *
 * With the exception of superscript, subscript, punctuation marks, and their
 * duplicates and variations, modifiers shall remain intact.
 *
 * <u>[U+0A720~U+0A7FF]</u>:
 *
 * Medieval abbreviations are treated likewise, because their expanded form
 * is often context-dependent. Non-ligatures, derived from ligatures but
 * resembling a single Latin letter, will expand to the former, even though
 * collation rules would place most of them immediately after the latter.
 *
 * <u>Spacing, Alignment, Punctuation</u>
 *
 * Includes:
 * - Apostrophes &amp; Quotes (Punctuation);
 * - Grave &amp; Acute accents (Punctuation);
 * - Colons, Hyphens &amp; Bullets (Punctuation);
 * - Commas, Periods &amp; Ellipses (Punctuation);
 * - Scores &amp; Macrons (Spacing);
 * - Question &amp; Exclamation marks (Punctuation); *2
 * - Exceptions to locale-invariance: *3
 *   - CJK &amp; Arabic (Alignment);
 *   - Ogham &amp; Mongolian (Spacing).
 *
 * Excludes:
 * - Asterisks, i.e. U+{0203B,02042,0204E,02051,02055,0205C};
 * - Obelisks, i.e. U+{02020,02021,02058,0205A,0205B,0205C,02E13};
 * - Tildes, i.e. U+02053 (Punctuation);
 * - Slashes, i.e. U+02044 (Punctuation);
 * - Carets, i.e. U+02038 (Spacing);
 * - Pipes, i.e. U+02016 (Spacing / Punctuation);
 * - Brackets, Parentheses &amp; Braces.
 *
 *
 * *1 Despite 'ampersand' technically being a ligature of [eT], its various
 *    incarnations shall remain intact.
 * *2 Interrobang U+0203D becomes &quot;?!&quot;, whereas
 *    inverted QE marks, i.e. U+{000A1,000BF}, become &quot;&quot;.
 * *3 All locale-specific Punctuation, e.g. U+{03001-03002,0300C-0300F},
 *    shall remain intact.
 * </pre>
 *
 * <pre>
 * [Summary]
 *
 * Constructed using
 *     http://www.unicode.org/ --> (for glyphs lacking font support)
 *     http://www.fileformat.info/info/unicode/
 *     http://codepoints.net/
 *     https://symbl.cc/
 *     http://seesaawiki.jp/w/qvarie/d/
 *     http://www.omniglot.com/writing/index.htm
 *     http://en.wikipedia.org/wiki/Latin_script_in_Unicode
 *
 * Included, but suspect:
 *     Latin Extended-D [U+0A720~U+0A7FF]
 *     U+02094 --> e (subscript schwa)
 *     U+02396 --> . (decimal mark)
 *     U+023AF --> - (h-line extension)
 *     U+1F1A5 --> d (squared letter)
 *
 * Excluded, too suspect:
 *     U+000A9 --> (C) (copyright sign)
 *     U+000AE --> (R) (registered sign)
 *     U+02051 --> ** (two vertical asterisks)
 *     U+02052 --> % (commercial minus sign)
 *     U+02120 --> SM (service-mark sign)
 *     U+02121 --> TEL (telephone sign)
 *     U+02122 --> TM (trade-mark sign)
 *     U+02145
 *      ~
 *     U+02149 --> (double-struck italics)
 *     U+0236E --> (semicolon underbar)
 *     U+02C75 --> H? (half H)
 *     U+02C76 --> h? (half h)
 *     U+02E33 --> . (raised dot)
 *     U+02E34 --> , (raised comma)
 *     U+0A7F7 --> I (epigraphic sideways I)
 *     U+1F12A --> [S] (used in baseball scoring)
 *     U+1F12B --> (C) (copyright sign)
 *     U+1F12C --> (R) (registered sign)
 *     U+1F12D ~
 *     U+1F12E --> (circled multi-letter)
 *     U+1F14A ~
 *     U+1F14F --> (squared multi-letter)
 *     U+1F18B ~
 *     U+1F1AD --> (squared multi-letter), except one
 *
 *     IPA extensions                 [U+00250,U+002AF]
 *     Spacing Modifier Letters       [U+002B0,U+002FF]
 *     Combining Diacritical Marks    [U+00300,U+0036F]
 *     Phonetic Extensions            [U+01D00,U+01D7F] *4
 *     Phonetic Extensions Supplement [U+01D80,U+01DBF]
 *     Letterlike Symbols             [U+02100,U+0214F] *5
 *     Number Forms                   [U+02150,U+0218F]
 *
 *     Auxiliary Latin glyphs derived from Greek/Egyptian glyphs
 *     (e.g. U+00194, U+01E9F, U+02C6D, U+0A722-0A725)
 *
 *     Mirrored glyphs and non-IPA Phonetic glyphs *6
 *     (e.g. U+0019C, U+001B9, U+001DD, U+0A78D, U+0AB31)
 *
 *     Manifesto languages
 *     (e.g. 'Volapuk' [U+0A79A,U+0A79F])
 *
 *
 * *4 With the exception of U+01D6B 'ue', because no alternative glyph is
 *    available in any other range.
 *
 * *5 With the exception of U+02114 'lb', which contracts to '#'.
 *
 * *6 With the exception of U+0018B-0018C 'D', which resembles U+00182 'B'.
 * </pre>
 *
 * @author hspereboom
 * @version 1.4 (2025.11 actualized to UCS-17)
 * @version 1.3 (2024.05 basic latin bypass &amp; IA5 buffer)
 * @version 1.2 (2024.01 punctuation corrections &amp; additions)
 * @version 1.1 (2023.12 actualized to UCS-15 &amp; made heap-friendly)
 * @version 1.0 (2023.11 template overhaul &amp; method overloads)
 * @version 0.3 (2021.11 notation &amp; rendering overhaul)
 * @version 0.2 (2018.xx additional symbols &amp; css tweaks)
 * @version 0.1 (2013.xx draft)
 */
@Generated("LatinDiacLiga.html")
public final class LatinDiacLiga {

	public static final int NO_AXES = 0;
	public static final int AXIS_LOWERCASE = 0x001;
	public static final int AXIS_UPPERCASE = 0x002;

	private LatinDiacLiga() {}

	/**
	 * @see {@link #asInvariantText(int, char[], int, int) asInvariantText(axis, data, 0, data.length)}.
	 */
	public static StringBuilder asInvariantText(final int axis, final char[] data) {
		return asInvariantText(axis, data, 0, data.length);
	}

	/**
	 * @return {@code null} if nothing changed; non-{@code null} otherwise.
	 *
	 * @see {@link #asInvariantText(StringBuilder, char[], int, char[], int, int) asInvariantText(…, …, axis, data, from, till)}.
	 */
	public static StringBuilder asInvariantText(final int axis, final char[] data, final int from, final int till) {
		final StringBuilder norm = new StringBuilder();
		final char[] temp = new char[4];

		return asInvariantText(norm, temp, axis, data, from, till) ? norm : null;
	}

	/**
	 * @see {@link #asInvariantText(StringBuilder, char[], int, char[], int, int) asInvariantText(norm, axis, data, 0, data.length)}.
	 */
	public static boolean asInvariantText(final StringBuilder norm, final char[] temp, final int axis, final char[] data) {
		return asInvariantText(norm, temp, axis, data, 0, data.length);
	}

	/**
	 * @param norm Assumed to be non-{@code null}.
	 * @param temp Assumed to be non-{@code null} and to have {@code .length >= 4}.
	 * @param axis
	 * @param data Assumed to be non-{@code null}.
	 * @param from Assumed to be valid; Inclusive.
	 * @param till Assumed to be valid; Exclusive;
	 *
	 * @return {@code true} if anything changed; {@code false} otherwise.
	 */
	public static boolean asInvariantText(final StringBuilder norm, final char[] temp, final int axis, final char[] data, final int from, final int till) {
		int ucp;
		boolean mod = false;

		for (int k = from, n = till; k < n; k += Character.charCount(ucp)) {
			mod |= cat(norm, axis, ucp = Character.codePointAt(data, k), temp);
		}

		return mod;
	}

	/**
	 * @see {@link #asInvariantText(int, String, int, int) asInvariantText(axis, data, 0, data.length())}.
	 */
	public static String asInvariantText(final int axis, final String data) {
		return asInvariantText(axis, data, 0, data.length());
	}

	/**
	 * @return {@code data} if nothing changed; non-{@code null} otherwise.
	 *
	 * @see {@link #asInvariantText(StringBuilder, char[], int, String, int, int) asInvariantText(…, …, axis, data, from, till)}.
	 */
	public static String asInvariantText(final int axis, final String data, final int from, final int till) {
		final StringBuilder norm = new StringBuilder();
		final char[] temp = new char[4];

		return asInvariantText(norm, temp, axis, data, from, till) ? norm.toString() : data;
	}

	/**
	 * @see {@link #asInvariantText(StringBuilder, char[], int, String, int, int) asInvariantText(norm, axis, data, 0, data.length())}.
	 */
	public static boolean asInvariantText(final StringBuilder norm, final char[] temp, final int axis, final String data) {
		return asInvariantText(norm, temp, axis, data, 0, data.length());
	}

	/**
	 * @param norm Assumed to be non-{@code null}.
	 * @param temp Assumed to be non-{@code null} and to have {@code .length >= 4}.
	 * @param axis
	 * @param data Assumed to be non-{@code null}.
	 * @param from Assumed to be valid; Inclusive.
	 * @param till Assumed to be valid; Exclusive;
	 *
	 * @return {@code true} if anything changed; {@code false} otherwise.
	 */
	public static boolean asInvariantText(final StringBuilder norm, final char[] temp, final int axis, final String data, final int from, final int till) {
		int ucp;
		boolean mod = false;

		for (int k = from, n = till; k < n; k += Character.charCount(ucp)) {
			mod |= cat(norm, axis, ucp = data.codePointAt(k), temp);
		}

		return mod;
	}

	/*
	 * Why not CharBuffer instead of StringBuilder:
	 * - It has static capacity, making it unsuitable for passing around;
	 * - It lacks appendCodePoint, making it unsuitable for mixed UCS/IA5.
	 */
	private static boolean cat(
		final StringBuilder norm,
		final int axis,
		final int ucp,
		final char[] ia5
	) {
		int seq;

		switch (seq = xl8(axis, ucp, ia5)) {
			case 1: case 2: case 3: case 4:
				norm.append(ia5, 0, seq);
			case 0:
				return true;
			default:
				norm.appendCodePoint(seq);
				return ucp != seq;
		}
	}

	/*
	 * Unicode blocks:
	 *   [U+00000~0007F] Basic Latin
	 *   [U+00080~000FF] Latin-1 Supplement
	 *   [U+00100~0017F] Latin Extended-A
	 *   [U+00180~0024F] Latin Extended-B
	 *   [U+002B0~002FF] Spacing Modifier Letters
	 *   [U+00600~006FF] Arabic
	 *   [U+00700~0074F] Syriac
	 *   [U+01400~0167F] Unified Canadian Aboriginal Syllabics
	 *   [U+01680~0169F] Ogham
	 *   [U+01800~018AF] Mongolian
	 *   [U+01D00~01D7F] Phonetic Extensions
	 *   [U+01E00~01EFF] Latin Extended Additional
	 *   [U+02000~0206F] General Punctuation
	 *   [U+02070~0209F] Superscripts and Subscripts
	 *   [U+02100~0214F] Letterlike Symbols
	 *   [U+02200~022FF] Mathematical Operators
	 *   [U+02300~023FF] Miscellaneous Technical
	 *   [U+02400~0243F] Control Pictures
	 *   [U+02460~024FF] Enclosed Alphanumerics
	 *   [U+025A0~025FF] Geometric Shapes
	 *   [U+02700~027BF] Dingbats
	 *   [U+02980~029FF] Miscellaneous Mathematical Symbols-B
	 *   [U+02A00~02AFF] Supplemental Mathematical Operators
	 *   [U+02C60~02C7F] Latin Extended-C
	 *   [U+02E00~02E7F] Supplemental Punctuation
	 *   [U+02E00~02E7F] Supplemental Punctuation
	 *   [U+03000~0303F] CJK Symbols and Punctuation
	 *   [U+030A0~030FF] Katakana
	 *   [U+03200~032FF] Enclosed CJK Letters and Months
	 *   [U+0A720~0A7FF] Latin Extended-D
	 *   [U+0AB30~0AB6F] Latin Extended-E
	 *   [U+0FB00~0FB4F] Alphabetic Presentation Forms
	 *   [U+0FE10~0FE1F] Vertical Forms
	 *   [U+0FE50~0FE6F] Small Form Variants
	 *   [U+0FE70~0FEFF] Arabic Presentation Forms-B
	 *   [U+0FF00~0FFEF] Halfwidth and Fullwidth Forms
	 *   [U+10100~1013F] Aegean Numbers
	 *   [U+10900~1091F] Phoenician
	 *   [U+10A00~10A5F] Kharoshthi
	 *   [U+10AC0~10AFF] Manichaean
	 *   [U+11000~1107F] Brahmi
	 *   [U+1CC00~1CEBF] Symbols for Legacy Computing Supplement
	 *   [U+1D400~1D7FF] Mathematical Alphanumeric Symbols
	 *   [U+1F100~1F1FF] Enclosed Alphanumeric Supplement
	 *   [U+1F650~1F67F] Ornamental Dingbats
	 *   [U+1FB00~1FBFF] Symbols for Legacy Computing
	 */
	private static int xl8(
		final int axis,
		final int ucp,
		final char[] ia5
	) {
		char ia5_0 = 0, ia5_1 = 0, ia5_2 = 0, ia5_3 = 0;

		do {
			//
			// NonGlyph
			//
			switch (ucp) {
				case 0x00000: case 0x00001: case 0x00002: case 0x00003: case 0x00004: case 0x00005: case 0x00006: case 0x00007:
				case 0x00008: case 0x0000E: case 0x0000F: case 0x00010: case 0x00011: case 0x00012: case 0x00013: case 0x00014:
				case 0x00015: case 0x00016: case 0x00017: case 0x00018: case 0x00019: case 0x0001A: case 0x0001B: case 0x0001C:
				case 0x0001D: case 0x0001E: case 0x0001F: case 0x0007F: case 0x00080: case 0x00081: case 0x00082: case 0x00083:
				case 0x00084: case 0x00086: case 0x00087: case 0x00088: case 0x00089: case 0x0008A: case 0x0008B: case 0x0008C:
				case 0x0008D: case 0x0008E: case 0x0008F: case 0x00090: case 0x00091: case 0x00092: case 0x00093: case 0x00094:
				case 0x00095: case 0x00096: case 0x00097: case 0x00098: case 0x00099: case 0x0009A: case 0x0009B: case 0x0009C:
				case 0x0009D: case 0x0009E: case 0x0009F: case 0x0180E: case 0x02400: case 0x02401: case 0x02402: case 0x02403:
				case 0x02404: case 0x02405: case 0x02406: case 0x02407: case 0x02408: case 0x02409: case 0x0240A: case 0x0240B:
				case 0x0240C: case 0x0240D: case 0x0240E: case 0x0240F: case 0x02410: case 0x02411: case 0x02412: case 0x02413:
				case 0x02414: case 0x02415: case 0x02416: case 0x02417: case 0x02418: case 0x02419: case 0x0241A: case 0x0241B:
				case 0x0241C: case 0x0241D: case 0x0241E: case 0x0241F: case 0x02421:
					return 0;
			}

			//
			// Diacritic
			//
			switch (ucp) {
				case 0x000C0: case 0x000C1: case 0x000C2: case 0x000C3: case 0x000C4: case 0x000C5: case 0x00100: case 0x00102:
				case 0x00104: case 0x001CD: case 0x001DE: case 0x001E0: case 0x001FA: case 0x00200: case 0x00202: case 0x00226:
				case 0x01E00: case 0x01EA0: case 0x01EA2: case 0x01EA4: case 0x01EA6: case 0x01EA8: case 0x01EAA: case 0x01EAC:
				case 0x01EAE: case 0x01EB0: case 0x01EB2: case 0x01EB4: case 0x01EB6:
					ia5_0 = 'A'; break;
				case 0x000C7: case 0x00106: case 0x00108: case 0x0010A: case 0x0010C: case 0x01E08:
					ia5_0 = 'C'; break;
				case 0x000C8: case 0x000C9: case 0x000CA: case 0x000CB: case 0x00112: case 0x00114: case 0x00116: case 0x00118:
				case 0x0011A: case 0x00204: case 0x00206: case 0x00228: case 0x01E14: case 0x01E16: case 0x01E18: case 0x01E1A:
				case 0x01E1C: case 0x01EB8: case 0x01EBA: case 0x01EBC: case 0x01EBE: case 0x01EC0: case 0x01EC2: case 0x01EC4:
				case 0x01EC6:
					ia5_0 = 'E'; break;
				case 0x000CC: case 0x000CD: case 0x000CE: case 0x000CF: case 0x00128: case 0x0012A: case 0x0012C: case 0x0012E:
				case 0x00130: case 0x001CF: case 0x00208: case 0x0020A: case 0x01E2C: case 0x01E2E: case 0x01EC8: case 0x01ECA:
					ia5_0 = 'I'; break;
				case 0x000D1: case 0x00143: case 0x00145: case 0x00147: case 0x001F8: case 0x01E44: case 0x01E46: case 0x01E48:
				case 0x01E4A:
					ia5_0 = 'N'; break;
				case 0x000D2: case 0x000D3: case 0x000D4: case 0x000D5: case 0x000D6: case 0x0014C: case 0x0014E: case 0x00150:
				case 0x001A0: case 0x001D1: case 0x001EA: case 0x001EC: case 0x0020C: case 0x0020E: case 0x0022A: case 0x0022C:
				case 0x0022E: case 0x00230: case 0x01E4C: case 0x01E4E: case 0x01E50: case 0x01E52: case 0x01ECC: case 0x01ECE:
				case 0x01ED0: case 0x01ED2: case 0x01ED4: case 0x01ED6: case 0x01ED8: case 0x01EDA: case 0x01EDC: case 0x01EDE:
				case 0x01EE0: case 0x01EE2:
					ia5_0 = 'O'; break;
				case 0x000D9: case 0x000DA: case 0x000DB: case 0x000DC: case 0x00168: case 0x0016A: case 0x0016C: case 0x0016E:
				case 0x00170: case 0x00172: case 0x001AF: case 0x001D3: case 0x001D5: case 0x001D7: case 0x001D9: case 0x001DB:
				case 0x00214: case 0x00216: case 0x01E72: case 0x01E74: case 0x01E76: case 0x01E78: case 0x01E7A: case 0x01EE4:
				case 0x01EE6: case 0x01EE8: case 0x01EEA: case 0x01EEC: case 0x01EEE: case 0x01EF0:
					ia5_0 = 'U'; break;
				case 0x000DD: case 0x00176: case 0x00178: case 0x00232: case 0x01E8E: case 0x01EF2: case 0x01EF4: case 0x01EF6:
				case 0x01EF8:
					ia5_0 = 'Y'; break;
				case 0x000E0: case 0x000E1: case 0x000E2: case 0x000E3: case 0x000E4: case 0x000E5: case 0x00101: case 0x00103:
				case 0x00105: case 0x001CE: case 0x001DF: case 0x001E1: case 0x001FB: case 0x00201: case 0x00203: case 0x00227:
				case 0x01E01: case 0x01EA1: case 0x01EA3: case 0x01EA5: case 0x01EA7: case 0x01EA9: case 0x01EAB: case 0x01EAD:
				case 0x01EAF: case 0x01EB1: case 0x01EB3: case 0x01EB5: case 0x01EB7:
					ia5_0 = 'a'; break;
				case 0x000E7: case 0x00107: case 0x00109: case 0x0010B: case 0x0010D: case 0x01E09:
					ia5_0 = 'c'; break;
				case 0x000E8: case 0x000E9: case 0x000EA: case 0x000EB: case 0x00113: case 0x00115: case 0x00117: case 0x00119:
				case 0x0011B: case 0x00205: case 0x00207: case 0x00229: case 0x01E15: case 0x01E17: case 0x01E19: case 0x01E1B:
				case 0x01E1D: case 0x01EB9: case 0x01EBB: case 0x01EBD: case 0x01EBF: case 0x01EC1: case 0x01EC3: case 0x01EC5:
				case 0x01EC7:
					ia5_0 = 'e'; break;
				case 0x000EC: case 0x000ED: case 0x000EE: case 0x000EF: case 0x00129: case 0x0012B: case 0x0012D: case 0x0012F:
				case 0x001D0: case 0x00209: case 0x0020B: case 0x01E2D: case 0x01E2F: case 0x01EC9: case 0x01ECB:
					ia5_0 = 'i'; break;
				case 0x000F1: case 0x00144: case 0x00146: case 0x00148: case 0x001F9: case 0x01E45: case 0x01E47: case 0x01E49:
				case 0x01E4B:
					ia5_0 = 'n'; break;
				case 0x000F2: case 0x000F3: case 0x000F4: case 0x000F5: case 0x000F6: case 0x0014D: case 0x0014F: case 0x00151:
				case 0x001A1: case 0x001D2: case 0x001EB: case 0x001ED: case 0x0020D: case 0x0020F: case 0x0022B: case 0x0022D:
				case 0x0022F: case 0x00231: case 0x01E4D: case 0x01E4F: case 0x01E51: case 0x01E53: case 0x01ECD: case 0x01ECF:
				case 0x01ED1: case 0x01ED3: case 0x01ED5: case 0x01ED7: case 0x01ED9: case 0x01EDB: case 0x01EDD: case 0x01EDF:
				case 0x01EE1: case 0x01EE3:
					ia5_0 = 'o'; break;
				case 0x000F9: case 0x000FA: case 0x000FB: case 0x000FC: case 0x00169: case 0x0016B: case 0x0016D: case 0x0016F:
				case 0x00171: case 0x00173: case 0x001B0: case 0x001D4: case 0x001D6: case 0x001D8: case 0x001DA: case 0x001DC:
				case 0x00215: case 0x00217: case 0x01E73: case 0x01E75: case 0x01E77: case 0x01E79: case 0x01E7B: case 0x01EE5:
				case 0x01EE7: case 0x01EE9: case 0x01EEB: case 0x01EED: case 0x01EEF: case 0x01EF1:
					ia5_0 = 'u'; break;
				case 0x000FD: case 0x000FF: case 0x00177: case 0x00233: case 0x01E8F: case 0x01E99: case 0x01EF3: case 0x01EF5:
				case 0x01EF7: case 0x01EF9:
					ia5_0 = 'y'; break;
				case 0x0010E: case 0x01E0A: case 0x01E0C: case 0x01E0E: case 0x01E10: case 0x01E12:
					ia5_0 = 'D'; break;
				case 0x0011C: case 0x0011E: case 0x00120: case 0x00122: case 0x001E6: case 0x001F4: case 0x01E20:
					ia5_0 = 'G'; break;
				case 0x00124: case 0x0021E: case 0x01E22: case 0x01E24: case 0x01E26: case 0x01E28: case 0x01E2A:
					ia5_0 = 'H'; break;
				case 0x00134:
					ia5_0 = 'J'; break;
				case 0x00136: case 0x001E8: case 0x01E30: case 0x01E32: case 0x01E34:
					ia5_0 = 'K'; break;
				case 0x00139: case 0x0013B: case 0x0013D: case 0x01E36: case 0x01E38: case 0x01E3A: case 0x01E3C:
					ia5_0 = 'L'; break;
				case 0x00154: case 0x00156: case 0x00158: case 0x00210: case 0x00212: case 0x01E58: case 0x01E5A: case 0x01E5C:
				case 0x01E5E:
					ia5_0 = 'R'; break;
				case 0x0015A: case 0x0015C: case 0x0015E: case 0x00160: case 0x00218: case 0x01E60: case 0x01E62: case 0x01E64:
				case 0x01E66: case 0x01E68:
					ia5_0 = 'S'; break;
				case 0x00162: case 0x00164: case 0x0021A: case 0x01E6A: case 0x01E6C: case 0x01E6E: case 0x01E70:
					ia5_0 = 'T'; break;
				case 0x00174: case 0x01E80: case 0x01E82: case 0x01E84: case 0x01E86: case 0x01E88:
					ia5_0 = 'W'; break;
				case 0x00179: case 0x0017B: case 0x0017D: case 0x01E90: case 0x01E92: case 0x01E94:
					ia5_0 = 'Z'; break;
				case 0x0010F: case 0x01E0B: case 0x01E0D: case 0x01E0F: case 0x01E11: case 0x01E13:
					ia5_0 = 'd'; break;
				case 0x0011D: case 0x0011F: case 0x00121: case 0x00123: case 0x001E7: case 0x001F5: case 0x01E21:
					ia5_0 = 'g'; break;
				case 0x00125: case 0x0021F: case 0x01E23: case 0x01E25: case 0x01E27: case 0x01E29: case 0x01E2B: case 0x01E96:
					ia5_0 = 'h'; break;
				case 0x00135: case 0x001F0:
					ia5_0 = 'j'; break;
				case 0x00137: case 0x001E9: case 0x01E31: case 0x01E33: case 0x01E35:
					ia5_0 = 'k'; break;
				case 0x0013A: case 0x0013C: case 0x0013E: case 0x01E37: case 0x01E39: case 0x01E3B: case 0x01E3D:
					ia5_0 = 'l'; break;
				case 0x00155: case 0x00157: case 0x00159: case 0x00211: case 0x00213: case 0x01E59: case 0x01E5B: case 0x01E5D:
				case 0x01E5F:
					ia5_0 = 'r'; break;
				case 0x0015B: case 0x0015D: case 0x0015F: case 0x00161: case 0x00219: case 0x01E61: case 0x01E63: case 0x01E65:
				case 0x01E67: case 0x01E69:
					ia5_0 = 's'; break;
				case 0x00163: case 0x00165: case 0x0021B: case 0x01E6B: case 0x01E6D: case 0x01E6F: case 0x01E71: case 0x01E97:
					ia5_0 = 't'; break;
				case 0x00175: case 0x01E81: case 0x01E83: case 0x01E85: case 0x01E87: case 0x01E89: case 0x01E98:
					ia5_0 = 'w'; break;
				case 0x0017A: case 0x0017C: case 0x0017E: case 0x01E91: case 0x01E93: case 0x01E95:
					ia5_0 = 'z'; break;
				case 0x01E02: case 0x01E04: case 0x01E06:
					ia5_0 = 'B'; break;
				case 0x01E1E:
					ia5_0 = 'F'; break;
				case 0x01E3E: case 0x01E40: case 0x01E42:
					ia5_0 = 'M'; break;
				case 0x01E54: case 0x01E56:
					ia5_0 = 'P'; break;
				case 0x01E7C: case 0x01E7E:
					ia5_0 = 'V'; break;
				case 0x01E8A: case 0x01E8C:
					ia5_0 = 'X'; break;
				case 0x01E03: case 0x01E05: case 0x01E07:
					ia5_0 = 'b'; break;
				case 0x01E1F:
					ia5_0 = 'f'; break;
				case 0x01E3F: case 0x01E41: case 0x01E43:
					ia5_0 = 'm'; break;
				case 0x01E55: case 0x01E57:
					ia5_0 = 'p'; break;
				case 0x01E7D: case 0x01E7F:
					ia5_0 = 'v'; break;
				case 0x01E8B: case 0x01E8D:
					ia5_0 = 'x'; break;
			}

			if (ia5_0 != 0) break;

			//
			// Ligature
			//
			switch (ucp) {
				case 0x000C6: case 0x001FC: case 0x001E2:
					ia5_0 = 'A'; ia5_1 = 'E'; break;
				case 0x000E6: case 0x001FD: case 0x001E3:
					ia5_0 = 'a'; ia5_1 = 'e'; break;
				case 0x000DF:
					ia5_0 = 's'; ia5_1 = 's'; break;
				case 0x000FE:
					ia5_0 = 't'; ia5_1 = 'h'; break;
				case 0x00132:
					ia5_0 = 'I'; ia5_1 = 'J'; break;
				case 0x00152:
					ia5_0 = 'O'; ia5_1 = 'E'; break;
				case 0x00133:
					ia5_0 = 'i'; ia5_1 = 'j'; break;
				case 0x00153:
					ia5_0 = 'o'; ia5_1 = 'e'; break;
				case 0x001A2:
					ia5_0 = 'O'; ia5_1 = 'I'; break;
				case 0x001C4: case 0x001F1:
					ia5_0 = 'D'; ia5_1 = 'Z'; break;
				case 0x001C7:
					ia5_0 = 'L'; ia5_1 = 'J'; break;
				case 0x001CA:
					ia5_0 = 'N'; ia5_1 = 'J'; break;
				case 0x001F6:
					ia5_0 = 'H'; ia5_1 = 'V'; break;
				case 0x00222:
					ia5_0 = 'O'; ia5_1 = 'U'; break;
				case 0x001C5: case 0x001F2:
					ia5_0 = 'D'; ia5_1 = 'z'; break;
				case 0x001C8:
					ia5_0 = 'L'; ia5_1 = 'j'; break;
				case 0x001CB:
					ia5_0 = 'N'; ia5_1 = 'j'; break;
				case 0x00195:
					ia5_0 = 'h'; ia5_1 = 'v'; break;
				case 0x001A3:
					ia5_0 = 'o'; ia5_1 = 'i'; break;
				case 0x001C6: case 0x001F3:
					ia5_0 = 'd'; ia5_1 = 'z'; break;
				case 0x001C9:
					ia5_0 = 'l'; ia5_1 = 'j'; break;
				case 0x001CC:
					ia5_0 = 'n'; ia5_1 = 'j'; break;
				case 0x00223:
					ia5_0 = 'o'; ia5_1 = 'u'; break;
				case 0x00238:
					ia5_0 = 'd'; ia5_1 = 'b'; break;
				case 0x00239:
					ia5_0 = 'q'; ia5_1 = 'p'; break;
				case 0x01E9E:
					ia5_0 = 'S'; ia5_1 = 'S'; break;
				case 0x01EFA:
					ia5_0 = 'L'; ia5_1 = 'L'; break;
				case 0x01EFB:
					ia5_0 = 'l'; ia5_1 = 'l'; break;
				case 0x0204A: case 0x02E52:
					ia5_0 = '&'; break;
				case 0x0A732:
					ia5_0 = 'A'; ia5_1 = 'A'; break;
				case 0x0A734:
					ia5_0 = 'A'; ia5_1 = 'O'; break;
				case 0x0A736:
					ia5_0 = 'A'; ia5_1 = 'U'; break;
				case 0x0A738: case 0x0A73A:
					ia5_0 = 'A'; ia5_1 = 'V'; break;
				case 0x0A73C:
					ia5_0 = 'A'; ia5_1 = 'Y'; break;
				case 0x0A74E:
					ia5_0 = 'O'; ia5_1 = 'O'; break;
				case 0x0A7D2:
					ia5_0 = 'T'; ia5_1 = 'H'; ia5_2 = 'T'; ia5_3 = 'H'; break;
				case 0x0A728:
					ia5_0 = 'T'; ia5_1 = 'Z'; break;
				case 0x0A7D4:
					ia5_0 = 'W'; ia5_1 = 'W'; break;
				case 0x0A733:
					ia5_0 = 'a'; ia5_1 = 'a'; break;
				case 0x0A735:
					ia5_0 = 'a'; ia5_1 = 'o'; break;
				case 0x0A737:
					ia5_0 = 'a'; ia5_1 = 'u'; break;
				case 0x0A739: case 0x0A73B:
					ia5_0 = 'a'; ia5_1 = 'v'; break;
				case 0x0A73D:
					ia5_0 = 'a'; ia5_1 = 'y'; break;
				case 0x0A74F:
					ia5_0 = 'o'; ia5_1 = 'o'; break;
				case 0x0A729:
					ia5_0 = 't'; ia5_1 = 'z'; break;
				case 0x0FB00:
					ia5_0 = 'f'; ia5_1 = 'f'; break;
				case 0x0FB01:
					ia5_0 = 'f'; ia5_1 = 'i'; break;
				case 0x0FB02:
					ia5_0 = 'f'; ia5_1 = 'l'; break;
				case 0x0FB03:
					ia5_0 = 'f'; ia5_1 = 'f'; ia5_2 = 'i'; break;
				case 0x0FB04:
					ia5_0 = 'f'; ia5_1 = 'f'; ia5_2 = 'l'; break;
				case 0x0FB05:
					ia5_0 = 'f'; ia5_1 = 't'; break;
				case 0x0FB06:
					ia5_0 = 's'; ia5_1 = 't'; break;
				case 0x01D6B:
					ia5_0 = 'u'; ia5_1 = 'e'; break;
			}

			if (ia5_0 != 0) break;

			//
			// SuoScript
			//
			switch (ucp) {
				case 0x000AA: case 0x02090:
					ia5_0 = 'a'; break;
				case 0x000BA: case 0x02092:
					ia5_0 = 'o'; break;
				case 0x02091: case 0x02094:
					ia5_0 = 'e'; break;
				case 0x02095:
					ia5_0 = 'h'; break;
				case 0x02071:
					ia5_0 = 'i'; break;
				case 0x02096:
					ia5_0 = 'k'; break;
				case 0x02097:
					ia5_0 = 'l'; break;
				case 0x02098:
					ia5_0 = 'm'; break;
				case 0x0207F: case 0x02099:
					ia5_0 = 'n'; break;
				case 0x0209A:
					ia5_0 = 'p'; break;
				case 0x0209B:
					ia5_0 = 's'; break;
				case 0x0209C:
					ia5_0 = 't'; break;
				case 0x02093:
					ia5_0 = 'x'; break;
				case 0x0A770:
					ia5_0 = 'c'; break;
				case 0x02C7C:
					ia5_0 = 'j'; break;
				case 0x02C7D:
					ia5_0 = 'v'; break;
			}

			if (ia5_0 != 0) break;

			//
			// Itemized
			//
			switch (ucp) {
				case 0x1CCF0:
					ia5_0 = '0'; break;
				case 0x1CCF1:
					ia5_0 = '1'; break;
				case 0x1CCF2:
					ia5_0 = '2'; break;
				case 0x1CCF3:
					ia5_0 = '3'; break;
				case 0x1CCF4:
					ia5_0 = '4'; break;
				case 0x1CCF5:
					ia5_0 = '5'; break;
				case 0x1CCF6:
					ia5_0 = '6'; break;
				case 0x1CCF7:
					ia5_0 = '7'; break;
				case 0x1CCF8:
					ia5_0 = '8'; break;
				case 0x1CCF9:
					ia5_0 = '9'; break;
				case 0x0249C: case 0x024D0:
					ia5_0 = 'a'; break;
				case 0x0249D: case 0x024D1:
					ia5_0 = 'b'; break;
				case 0x0249E: case 0x024D2:
					ia5_0 = 'c'; break;
				case 0x0249F: case 0x024D3: case 0x1F1A5:
					ia5_0 = 'd'; break;
				case 0x024A0: case 0x024D4:
					ia5_0 = 'e'; break;
				case 0x024A1: case 0x024D5:
					ia5_0 = 'f'; break;
				case 0x024A2: case 0x024D6:
					ia5_0 = 'g'; break;
				case 0x024A3: case 0x024D7:
					ia5_0 = 'h'; break;
				case 0x024A4: case 0x024D8:
					ia5_0 = 'i'; break;
				case 0x024A5: case 0x024D9:
					ia5_0 = 'j'; break;
				case 0x024A6: case 0x024DA:
					ia5_0 = 'k'; break;
				case 0x024A7: case 0x024DB:
					ia5_0 = 'l'; break;
				case 0x024A8: case 0x024DC:
					ia5_0 = 'm'; break;
				case 0x024A9: case 0x024DD:
					ia5_0 = 'n'; break;
				case 0x024AA: case 0x024DE:
					ia5_0 = 'o'; break;
				case 0x024AB: case 0x024DF:
					ia5_0 = 'p'; break;
				case 0x024AC: case 0x024E0:
					ia5_0 = 'q'; break;
				case 0x024AD: case 0x024E1:
					ia5_0 = 'r'; break;
				case 0x024AE: case 0x024E2:
					ia5_0 = 's'; break;
				case 0x024AF: case 0x024E3:
					ia5_0 = 't'; break;
				case 0x024B0: case 0x024E4:
					ia5_0 = 'u'; break;
				case 0x024B1: case 0x024E5:
					ia5_0 = 'v'; break;
				case 0x024B2: case 0x024E6:
					ia5_0 = 'w'; break;
				case 0x024B3: case 0x024E7:
					ia5_0 = 'x'; break;
				case 0x024B4: case 0x024E8:
					ia5_0 = 'y'; break;
				case 0x024B5: case 0x024E9:
					ia5_0 = 'z'; break;
				case 0x024B6: case 0x1CCD6: case 0x1F110: case 0x1F130: case 0x1F150: case 0x1F170: case 0x1F1E6:
					ia5_0 = 'A'; break;
				case 0x024B7: case 0x1CCD7: case 0x1F111: case 0x1F131: case 0x1F151: case 0x1F171: case 0x1F1E7:
					ia5_0 = 'B'; break;
				case 0x024B8: case 0x1CCD8: case 0x1F112: case 0x1F132: case 0x1F152: case 0x1F172: case 0x1F1E8:
					ia5_0 = 'C'; break;
				case 0x024B9: case 0x1CCD9: case 0x1F113: case 0x1F133: case 0x1F153: case 0x1F173: case 0x1F1E9:
					ia5_0 = 'D'; break;
				case 0x024BA: case 0x1CCDA: case 0x1F114: case 0x1F134: case 0x1F154: case 0x1F174: case 0x1F1EA:
					ia5_0 = 'E'; break;
				case 0x024BB: case 0x1CCDB: case 0x1F115: case 0x1F135: case 0x1F155: case 0x1F175: case 0x1F1EB:
					ia5_0 = 'F'; break;
				case 0x024BC: case 0x1CCDC: case 0x1F116: case 0x1F136: case 0x1F156: case 0x1F176: case 0x1F1EC:
					ia5_0 = 'G'; break;
				case 0x024BD: case 0x1CCDD: case 0x1F117: case 0x1F137: case 0x1F157: case 0x1F177: case 0x1F1ED:
					ia5_0 = 'H'; break;
				case 0x024BE: case 0x1CCDE: case 0x1F118: case 0x1F138: case 0x1F158: case 0x1F178: case 0x1F1EE:
					ia5_0 = 'I'; break;
				case 0x024BF: case 0x1CCDF: case 0x1F119: case 0x1F139: case 0x1F159: case 0x1F179: case 0x1F1EF:
					ia5_0 = 'J'; break;
				case 0x024C0: case 0x1CCE0: case 0x1F11A: case 0x1F13A: case 0x1F15A: case 0x1F17A: case 0x1F1F0:
					ia5_0 = 'K'; break;
				case 0x024C1: case 0x1CCE1: case 0x1F11B: case 0x1F13B: case 0x1F15B: case 0x1F17B: case 0x1F1F1:
					ia5_0 = 'L'; break;
				case 0x024C2: case 0x1CCE2: case 0x1F11C: case 0x1F13C: case 0x1F15C: case 0x1F17C: case 0x1F1F2:
					ia5_0 = 'M'; break;
				case 0x024C3: case 0x1CCE3: case 0x1F11D: case 0x1F13D: case 0x1F15D: case 0x1F17D: case 0x1F1F3:
					ia5_0 = 'N'; break;
				case 0x024C4: case 0x1CCE4: case 0x1F11E: case 0x1F13E: case 0x1F15E: case 0x1F17E: case 0x1F1F4:
					ia5_0 = 'O'; break;
				case 0x024C5: case 0x1CCE5: case 0x1F11F: case 0x1F13F: case 0x1F15F: case 0x1F17F: case 0x1F1F5:
					ia5_0 = 'P'; break;
				case 0x024C6: case 0x1CCE6: case 0x1F120: case 0x1F140: case 0x1F160: case 0x1F180: case 0x1F1F6:
					ia5_0 = 'Q'; break;
				case 0x024C7: case 0x1CCE7: case 0x1F121: case 0x1F141: case 0x1F161: case 0x1F181: case 0x1F1F7:
					ia5_0 = 'R'; break;
				case 0x024C8: case 0x1CCE8: case 0x1F122: case 0x1F142: case 0x1F162: case 0x1F182: case 0x1F1F8:
					ia5_0 = 'S'; break;
				case 0x024C9: case 0x1CCE9: case 0x1F123: case 0x1F143: case 0x1F163: case 0x1F183: case 0x1F1F9:
					ia5_0 = 'T'; break;
				case 0x024CA: case 0x1CCEA: case 0x1F124: case 0x1F144: case 0x1F164: case 0x1F184: case 0x1F1FA:
					ia5_0 = 'U'; break;
				case 0x024CB: case 0x1CCEB: case 0x1F125: case 0x1F145: case 0x1F165: case 0x1F185: case 0x1F1FB:
					ia5_0 = 'V'; break;
				case 0x024CC: case 0x1CCEC: case 0x1F126: case 0x1F146: case 0x1F166: case 0x1F186: case 0x1F1FC:
					ia5_0 = 'W'; break;
				case 0x024CD: case 0x1CCED: case 0x1F127: case 0x1F147: case 0x1F167: case 0x1F187: case 0x1F1FD:
					ia5_0 = 'X'; break;
				case 0x024CE: case 0x1CCEE: case 0x1F128: case 0x1F148: case 0x1F168: case 0x1F188: case 0x1F1FE:
					ia5_0 = 'Y'; break;
				case 0x024CF: case 0x1CCEF: case 0x1F129: case 0x1F149: case 0x1F169: case 0x1F189: case 0x1F1FF:
					ia5_0 = 'Z'; break;
			}

			if (ia5_0 != 0) break;

			//
			// Adopted
			//
			switch (ucp) {
				case 0x000D8: case 0x00186: case 0x0019F: case 0x001FE: case 0x0A74A: case 0x0A74C: case 0x0A7C0:
					ia5_0 = 'O'; break;
				case 0x000F8: case 0x001FF: case 0x02C7A: case 0x0A74B: case 0x0A74D: case 0x0A7C1: case 0x0AB3D: case 0x0AB3E:
				case 0x0AB3F:
					ia5_0 = 'o'; break;
				case 0x000D0:
					ia5_0 = 'D'; ia5_1 = 'H'; break;
				case 0x000DE: case 0x0A764: case 0x0A766:
					ia5_0 = 'T'; ia5_1 = 'H'; break;
				case 0x000F0:
					ia5_0 = 'd'; ia5_1 = 'h'; break;
				case 0x00041: case 0x00042: case 0x00043: case 0x00044: case 0x00045: case 0x00046: case 0x00047: case 0x00048:
				case 0x00049: case 0x0004A: case 0x0004B: case 0x0004C: case 0x0004D: case 0x0004E: case 0x0004F: case 0x00050:
				case 0x00051: case 0x00052: case 0x00053: case 0x00054: case 0x00055: case 0x00056: case 0x00057: case 0x00058:
				case 0x00059: case 0x0005A: case 0x00061: case 0x00062: case 0x00063: case 0x00064: case 0x00065: case 0x00066:
				case 0x00067: case 0x00068: case 0x00069: case 0x0006A: case 0x0006B: case 0x0006C: case 0x0006D: case 0x0006E:
				case 0x0006F: case 0x00070: case 0x00071: case 0x00072: case 0x00073: case 0x00074: case 0x00075: case 0x00076:
				case 0x00077: case 0x00078: case 0x00079: case 0x0007A:
					continue;
				case 0x00110: case 0x00189: case 0x0018A: case 0x0018B: case 0x0A779: case 0x0A7C7:
					ia5_0 = 'D'; break;
				case 0x00126: case 0x02C67: case 0x0A726: case 0x0A7AA:
					ia5_0 = 'H'; break;
				case 0x0013F: case 0x00141: case 0x0023D: case 0x02C60: case 0x02C62: case 0x0A748: case 0x0A780: case 0x0A7AD:
					ia5_0 = 'L'; break;
				case 0x00166: case 0x001AC: case 0x001AE: case 0x0023E: case 0x0A786:
					ia5_0 = 'T'; break;
				case 0x00111: case 0x0018C: case 0x00221: case 0x0A771: case 0x0A77A: case 0x0A7C8:
					ia5_0 = 'd'; break;
				case 0x00127: case 0x02C68: case 0x0A727: case 0x0A795:
					ia5_0 = 'h'; break;
				case 0x00131:
					ia5_0 = 'i'; break;
				case 0x00138: case 0x00199: case 0x02C6A: case 0x0A741: case 0x0A743: case 0x0A745: case 0x0A7A3:
					ia5_0 = 'k'; break;
				case 0x00140: case 0x00142: case 0x0019A: case 0x00234: case 0x02C61: case 0x0A749: case 0x0A772: case 0x0A781:
				case 0x0AB37: case 0x0AB38: case 0x0AB39:
					ia5_0 = 'l'; break;
				case 0x0017F: case 0x0023F: case 0x01E9B: case 0x01E9C: case 0x01E9D: case 0x0A76D: case 0x0A778: case 0x0A785:
				case 0x0A7A9: case 0x0A7CA: case 0x0A7D7: case 0x0A7D9: case 0x0AB4D:
					ia5_0 = 's'; break;
				case 0x00167: case 0x001AB: case 0x001AD: case 0x00236: case 0x02C66: case 0x0A777: case 0x0A787:
					ia5_0 = 't'; break;
				case 0x0014A:
					ia5_0 = 'N'; ia5_1 = 'G'; break;
				case 0x0014B: case 0x0AB3C:
					ia5_0 = 'n'; ia5_1 = 'g'; break;
				case 0x00149:
					ia5_0 = '\''; ia5_1 = 'n'; break;
				case 0x0023A:
					ia5_0 = 'A'; break;
				case 0x00181: case 0x00182: case 0x00243: case 0x0A796:
					ia5_0 = 'B'; break;
				case 0x00187: case 0x0023B: case 0x0A76E: case 0x0A792: case 0x0A73E:
					ia5_0 = 'C'; break;
				case 0x00246: case 0x0A76A: case 0x0A7AB:
					ia5_0 = 'E'; break;
				case 0x00191: case 0x0A77B: case 0x0A798:
					ia5_0 = 'F'; break;
				case 0x00193: case 0x001E4: case 0x0A77D: case 0x0A77E: case 0x0A7A0: case 0x0A7AC: case 0x0A7D0:
					ia5_0 = 'G'; break;
				case 0x00197: case 0x0A7AE:
					ia5_0 = 'I'; break;
				case 0x00248:
					ia5_0 = 'J'; break;
				case 0x00198: case 0x02C69: case 0x0A740: case 0x0A742: case 0x0A744: case 0x0A7A2:
					ia5_0 = 'K'; break;
				case 0x0019D: case 0x00220: case 0x0A790: case 0x0A7A4:
					ia5_0 = 'N'; break;
				case 0x001A4: case 0x02C63: case 0x0A750: case 0x0A752: case 0x0A754:
					ia5_0 = 'P'; break;
				case 0x0024A: case 0x0A756: case 0x0A758:
					ia5_0 = 'Q'; break;
				case 0x001A6: case 0x0024C: case 0x02C64: case 0x0A75A: case 0x0A75C: case 0x0A776: case 0x0A782: case 0x0A7A6:
				case 0x0AB46:
					ia5_0 = 'R'; break;
				case 0x00244: case 0x0A7B8:
					ia5_0 = 'U'; break;
				case 0x001B2: case 0x0A75E: case 0x0A768:
					ia5_0 = 'V'; break;
				case 0x001F7: case 0x01EFC: case 0x02C72: case 0x0A7C2:
					ia5_0 = 'W'; break;
				case 0x0024E: case 0x01EFE:
					ia5_0 = 'Y'; break;
				case 0x001B5: case 0x00224: case 0x02C6B: case 0x02C7F: case 0x0A762:
					ia5_0 = 'Z'; break;
				case 0x00180: case 0x00183: case 0x0A797:
					ia5_0 = 'b'; break;
				case 0x00188: case 0x0023C: case 0x0A73F: case 0x0A76F: case 0x0A793: case 0x0A794:
					ia5_0 = 'c'; break;
				case 0x00247: case 0x02C78: case 0x0A76B: case 0x0AB32: case 0x0AB33: case 0x0AB34:
					ia5_0 = 'e'; break;
				case 0x00192: case 0x0A77C: case 0x0A799: case 0x0AB35:
					ia5_0 = 'f'; break;
				case 0x001E5: case 0x0A77F: case 0x0A7A1: case 0x0A7D1: case 0x0AB36:
					ia5_0 = 'g'; break;
				case 0x00237: case 0x00249:
					ia5_0 = 'j'; break;
				case 0x0019E: case 0x00235: case 0x0A774: case 0x0A791: case 0x0A7A5: case 0x0AB3B:
					ia5_0 = 'n'; break;
				case 0x001A5: case 0x0A751: case 0x0A753: case 0x0A755:
					ia5_0 = 'p'; break;
				case 0x0024B: case 0x0A757: case 0x0A759:
					ia5_0 = 'q'; break;
				case 0x0024D: case 0x0A75B: case 0x0A75D: case 0x0A775: case 0x0A783: case 0x0A7A7: case 0x0AB47: case 0x0AB49:
				case 0x0AB4B: case 0x0AB4C:
					ia5_0 = 'r'; break;
				case 0x001BF: case 0x01EFD: case 0x02C73: case 0x0A7C3: case 0x0A7D5:
					ia5_0 = 'w'; break;
				case 0x0024F: case 0x01EFF: case 0x0AB5A:
					ia5_0 = 'y'; break;
				case 0x001B6: case 0x00225: case 0x00240: case 0x02C6C: case 0x0A763:
					ia5_0 = 'z'; break;
				case 0x001B7: case 0x001EE:
					ia5_0 = 'Z'; ia5_1 = 'H'; break;
				case 0x0021C:
					ia5_0 = 'G'; ia5_1 = 'H'; break;
				case 0x001BA: case 0x001EF:
					ia5_0 = 'z'; ia5_1 = 'h'; break;
				case 0x0021D:
					ia5_0 = 'g'; ia5_1 = 'h'; break;
				case 0x001B3:
					ia5_0 = '\''; ia5_1 = 'Y'; break;
				case 0x001B4:
					ia5_0 = '\''; ia5_1 = 'y'; break;
				case 0x01E9A: case 0x02C65: case 0x0AB30:
					ia5_0 = 'a'; break;
				case 0x02C6E:
					ia5_0 = 'M'; break;
				case 0x02C7E: case 0x0A76C: case 0x0A784: case 0x0A7A8: case 0x0A7C9: case 0x0A7CC: case 0x0A7CD: case 0x0A7D6:
				case 0x0A7D8:
					ia5_0 = 'S'; break;
				case 0x0A773: case 0x0AB3A:
					ia5_0 = 'm'; break;
				case 0x0A7B9: case 0x0AB4E: case 0x0AB4F: case 0x0AB52:
					ia5_0 = 'u'; break;
				case 0x02C71: case 0x02C74: case 0x0A75F: case 0x0A769:
					ia5_0 = 'v'; break;
				case 0x0AB56: case 0x0AB57: case 0x0AB58: case 0x0AB59:
					ia5_0 = 'x'; break;
				case 0x0A746:
					ia5_0 = 'L'; ia5_1 = 'L'; break;
				case 0x0A760:
					ia5_0 = 'V'; ia5_1 = 'Y'; break;
				case 0x0AB61:
					ia5_0 = 'i'; ia5_1 = 'e'; break;
				case 0x0AB62:
					ia5_0 = 'o'; ia5_1 = 'e'; break;
				case 0x0A747: case 0x0A78E:
					ia5_0 = 'l'; ia5_1 = 'l'; break;
				case 0x0AB48: case 0x0AB4A:
					ia5_0 = 'r'; ia5_1 = 'r'; break;
				case 0x0A765: case 0x0A767: case 0x0A7D3:
					ia5_0 = 't'; ia5_1 = 'h'; break;
				case 0x0AB50: case 0x0AB51:
					ia5_0 = 'u'; ia5_1 = 'i'; break;
				case 0x0AB63:
					ia5_0 = 'u'; ia5_1 = 'o'; break;
				case 0x0A761:
					ia5_0 = 'v'; ia5_1 = 'y'; break;
			}

			if (ia5_0 != 0) break;

			//
			// Numeric
			//
			switch (ucp) {
				case 0x02070: case 0x02080: case 0x03007: case 0x024EA: case 0x024FF: case 0x1D7CE: case 0x1D7D8: case 0x1D7E2:
				case 0x1D7EC: case 0x1D7F6: case 0x1F100: case 0x1F101: case 0x1F10B: case 0x1F10C: case 0x1FBF0: case 0x0FF10:
					ia5_0 = '0'; break;
				case 0x000B9: case 0x02081: case 0x02460: case 0x02474: case 0x02488: case 0x024F5: case 0x1D7CF: case 0x1D7D9:
				case 0x1D7E3: case 0x1D7ED: case 0x1D7F7: case 0x1F102: case 0x1FBF1: case 0x0FF11: case 0x02776: case 0x02780:
				case 0x0278A:
					ia5_0 = '1'; break;
				case 0x000B2: case 0x001A7: case 0x001A8: case 0x001BB: case 0x02082: case 0x02461: case 0x02475: case 0x02489:
				case 0x024F6: case 0x1D7D0: case 0x1D7DA: case 0x1D7E4: case 0x1D7EE: case 0x1D7F8: case 0x1F103: case 0x1FBF2:
				case 0x0FF12: case 0x02777: case 0x02781: case 0x0278B:
					ia5_0 = '2'; break;
				case 0x000B3: case 0x02083: case 0x02462: case 0x02476: case 0x0248A: case 0x024F7: case 0x1D7D1: case 0x1D7DB:
				case 0x1D7E5: case 0x1D7EF: case 0x1D7F9: case 0x1F104: case 0x1FBF3: case 0x0A72A: case 0x0A72B: case 0x0FF13:
				case 0x02778: case 0x02782: case 0x0278C:
					ia5_0 = '3'; break;
				case 0x02074: case 0x02084: case 0x02463: case 0x02477: case 0x0248B: case 0x024F8: case 0x1D7D2: case 0x1D7DC:
				case 0x1D7E6: case 0x1D7F0: case 0x1D7FA: case 0x1F105: case 0x1FBF4: case 0x0A72C: case 0x0A72D: case 0x0A72E:
				case 0x0A72F: case 0x0FF14: case 0x02779: case 0x02783: case 0x0278D:
					ia5_0 = '4'; break;
				case 0x001BC: case 0x001BD: case 0x02075: case 0x02085: case 0x02464: case 0x02478: case 0x0248C: case 0x024F9:
				case 0x1D7D3: case 0x1D7DD: case 0x1D7E7: case 0x1D7F1: case 0x1D7FB: case 0x1F106: case 0x1FBF5: case 0x0FF15:
				case 0x0277A: case 0x02784: case 0x0278E:
					ia5_0 = '5'; break;
				case 0x00184: case 0x00185: case 0x02076: case 0x02086: case 0x02465: case 0x02479: case 0x0248D: case 0x024FA:
				case 0x1D7D4: case 0x1D7DE: case 0x1D7E8: case 0x1D7F2: case 0x1D7FC: case 0x1F107: case 0x1FBF6: case 0x0FF16:
				case 0x0277B: case 0x02785: case 0x0278F:
					ia5_0 = '6'; break;
				case 0x02077: case 0x02087: case 0x02466: case 0x0247A: case 0x0248E: case 0x024FB: case 0x1D7D5: case 0x1D7DF:
				case 0x1D7E9: case 0x1D7F3: case 0x1D7FD: case 0x1F108: case 0x1FBF7: case 0x0FF17: case 0x0277C: case 0x02786:
				case 0x02790:
					ia5_0 = '7'; break;
				case 0x02078: case 0x02088: case 0x02467: case 0x0247B: case 0x0248F: case 0x024FC: case 0x1D7D6: case 0x1D7E0:
				case 0x1D7EA: case 0x1D7F4: case 0x1D7FE: case 0x1F109: case 0x1FBF8: case 0x0FF18: case 0x0277D: case 0x02787:
				case 0x02791:
					ia5_0 = '8'; break;
				case 0x02079: case 0x02089: case 0x02468: case 0x0247C: case 0x02490: case 0x024FD: case 0x1D7D7: case 0x1D7E1:
				case 0x1D7EB: case 0x1D7F5: case 0x1D7FF: case 0x1F10A: case 0x1FBF9: case 0x0FF19: case 0x0277E: case 0x02788:
				case 0x02792:
					ia5_0 = '9'; break;
				case 0x02469: case 0x0247D: case 0x02491: case 0x024FE: case 0x03248: case 0x0277F: case 0x02789: case 0x02793:
					ia5_0 = '1'; ia5_1 = '0'; break;
				case 0x0246A: case 0x0247E: case 0x02492: case 0x024EB:
					ia5_0 = '1'; ia5_1 = '1'; break;
				case 0x0246B: case 0x0247F: case 0x02493: case 0x024EC:
					ia5_0 = '1'; ia5_1 = '2'; break;
				case 0x0246C: case 0x02480: case 0x02494: case 0x024ED:
					ia5_0 = '1'; ia5_1 = '3'; break;
				case 0x0246D: case 0x02481: case 0x02495: case 0x024EE:
					ia5_0 = '1'; ia5_1 = '4'; break;
				case 0x0246E: case 0x02482: case 0x02496: case 0x024EF:
					ia5_0 = '1'; ia5_1 = '5'; break;
				case 0x0246F: case 0x02483: case 0x02497: case 0x024F0:
					ia5_0 = '1'; ia5_1 = '6'; break;
				case 0x02470: case 0x02484: case 0x02498: case 0x024F1:
					ia5_0 = '1'; ia5_1 = '7'; break;
				case 0x02471: case 0x02485: case 0x02499: case 0x024F2:
					ia5_0 = '1'; ia5_1 = '8'; break;
				case 0x02472: case 0x02486: case 0x0249A: case 0x024F3:
					ia5_0 = '1'; ia5_1 = '9'; break;
				case 0x02473: case 0x02487: case 0x0249B: case 0x024F4: case 0x03249:
					ia5_0 = '2'; ia5_1 = '0'; break;
				case 0x03251:
					ia5_0 = '2'; ia5_1 = '1'; break;
				case 0x03252:
					ia5_0 = '2'; ia5_1 = '2'; break;
				case 0x03253:
					ia5_0 = '2'; ia5_1 = '3'; break;
				case 0x03254:
					ia5_0 = '2'; ia5_1 = '4'; break;
				case 0x03255:
					ia5_0 = '2'; ia5_1 = '5'; break;
				case 0x03256:
					ia5_0 = '2'; ia5_1 = '6'; break;
				case 0x03257:
					ia5_0 = '2'; ia5_1 = '7'; break;
				case 0x03258:
					ia5_0 = '2'; ia5_1 = '8'; break;
				case 0x03259:
					ia5_0 = '2'; ia5_1 = '9'; break;
				case 0x0324A: case 0x0325A:
					ia5_0 = '3'; ia5_1 = '0'; break;
				case 0x0325B:
					ia5_0 = '3'; ia5_1 = '1'; break;
				case 0x0325C:
					ia5_0 = '3'; ia5_1 = '2'; break;
				case 0x0325D:
					ia5_0 = '3'; ia5_1 = '3'; break;
				case 0x0325E:
					ia5_0 = '3'; ia5_1 = '4'; break;
				case 0x0325F:
					ia5_0 = '3'; ia5_1 = '5'; break;
				case 0x032B1:
					ia5_0 = '3'; ia5_1 = '6'; break;
				case 0x032B2:
					ia5_0 = '3'; ia5_1 = '7'; break;
				case 0x032B3:
					ia5_0 = '3'; ia5_1 = '8'; break;
				case 0x032B4:
					ia5_0 = '3'; ia5_1 = '9'; break;
				case 0x0324B: case 0x032B5:
					ia5_0 = '4'; ia5_1 = '0'; break;
				case 0x032B6:
					ia5_0 = '4'; ia5_1 = '1'; break;
				case 0x032B7:
					ia5_0 = '4'; ia5_1 = '2'; break;
				case 0x032B8:
					ia5_0 = '4'; ia5_1 = '3'; break;
				case 0x032B9:
					ia5_0 = '4'; ia5_1 = '4'; break;
				case 0x032BA:
					ia5_0 = '4'; ia5_1 = '5'; break;
				case 0x032BB:
					ia5_0 = '4'; ia5_1 = '6'; break;
				case 0x032BC:
					ia5_0 = '4'; ia5_1 = '7'; break;
				case 0x032BD:
					ia5_0 = '4'; ia5_1 = '8'; break;
				case 0x032BE:
					ia5_0 = '4'; ia5_1 = '9'; break;
				case 0x0324C: case 0x032BF:
					ia5_0 = '5'; ia5_1 = '0'; break;
				case 0x0324D:
					ia5_0 = '6'; ia5_1 = '0'; break;
				case 0x0324E:
					ia5_0 = '7'; ia5_1 = '0'; break;
				case 0x0324F:
					ia5_0 = '8'; ia5_1 = '0'; break;
				case 0x00030: case 0x00031: case 0x00032: case 0x00033: case 0x00034: case 0x00035: case 0x00036: case 0x00037:
				case 0x00038: case 0x00039:
					continue;
			}

			if (ia5_0 != 0) break;

			//
			// Spacing
			//
			switch (ucp) {
				case 0x0000D: case 0x0200B: case 0x0200C: case 0x0200D: case 0x0FEFF:
					return 0;
				case 0x0000A: case 0x0000B: case 0x0000C: case 0x00085: case 0x02028: case 0x02029: case 0x02424:
					ia5_0 = '\n'; break;
				case 0x00009: case 0x000A0: case 0x02000: case 0x02001: case 0x02002: case 0x02003: case 0x02004: case 0x02005:
				case 0x02006: case 0x02007: case 0x02008: case 0x02009: case 0x0200A: case 0x0202F: case 0x0205F: case 0x02060:
				case 0x03000: case 0x01680: case 0x0237D: case 0x02420: case 0x02422: case 0x02423:
					ia5_0 = ' '; break;
				case 0x00020:
					continue;
			}

			if (ia5_0 != 0) break;

			//
			// Alignment
			//
			switch (ucp) {
				case 0x0A730: case 0x0FF26:
					ia5_0 = 'F'; break;
				case 0x0A7FE: case 0x0FF29:
					ia5_0 = 'I'; break;
				case 0x0A7FF: case 0x0FF2D:
					ia5_0 = 'M'; break;
				case 0x0A731: case 0x0FF33:
					ia5_0 = 'S'; break;
				case 0x0FF21:
					ia5_0 = 'A'; break;
				case 0x0FF22:
					ia5_0 = 'B'; break;
				case 0x0FF23:
					ia5_0 = 'C'; break;
				case 0x0FF24:
					ia5_0 = 'D'; break;
				case 0x0FF25:
					ia5_0 = 'E'; break;
				case 0x0FF27:
					ia5_0 = 'G'; break;
				case 0x0FF28:
					ia5_0 = 'H'; break;
				case 0x0FF2A:
					ia5_0 = 'J'; break;
				case 0x0FF2B:
					ia5_0 = 'K'; break;
				case 0x0FF2C:
					ia5_0 = 'L'; break;
				case 0x0FF2E:
					ia5_0 = 'N'; break;
				case 0x0FF2F:
					ia5_0 = 'O'; break;
				case 0x0FF30:
					ia5_0 = 'P'; break;
				case 0x0FF31:
					ia5_0 = 'Q'; break;
				case 0x0FF32:
					ia5_0 = 'R'; break;
				case 0x0FF34:
					ia5_0 = 'T'; break;
				case 0x0FF35:
					ia5_0 = 'U'; break;
				case 0x0FF36:
					ia5_0 = 'V'; break;
				case 0x0FF37:
					ia5_0 = 'W'; break;
				case 0x0FF38:
					ia5_0 = 'X'; break;
				case 0x0FF39:
					ia5_0 = 'Y'; break;
				case 0x0FF3A:
					ia5_0 = 'Z'; break;
				case 0x0FF41:
					ia5_0 = 'a'; break;
				case 0x0FF42:
					ia5_0 = 'b'; break;
				case 0x0FF43:
					ia5_0 = 'c'; break;
				case 0x0FF44:
					ia5_0 = 'd'; break;
				case 0x0FF45:
					ia5_0 = 'e'; break;
				case 0x0FF46:
					ia5_0 = 'f'; break;
				case 0x0FF47:
					ia5_0 = 'g'; break;
				case 0x0FF48:
					ia5_0 = 'h'; break;
				case 0x0FF49:
					ia5_0 = 'i'; break;
				case 0x0FF4A:
					ia5_0 = 'j'; break;
				case 0x0FF4B:
					ia5_0 = 'k'; break;
				case 0x0FF4C:
					ia5_0 = 'l'; break;
				case 0x0FF4D:
					ia5_0 = 'm'; break;
				case 0x0FF4E:
					ia5_0 = 'n'; break;
				case 0x0FF4F:
					ia5_0 = 'o'; break;
				case 0x0FF50:
					ia5_0 = 'p'; break;
				case 0x0FF51:
					ia5_0 = 'q'; break;
				case 0x0FF52:
					ia5_0 = 'r'; break;
				case 0x0FF53:
					ia5_0 = 's'; break;
				case 0x0FF54:
					ia5_0 = 't'; break;
				case 0x0FF55:
					ia5_0 = 'u'; break;
				case 0x0FF56:
					ia5_0 = 'v'; break;
				case 0x0FF57:
					ia5_0 = 'w'; break;
				case 0x0FF58:
					ia5_0 = 'x'; break;
				case 0x0FF59:
					ia5_0 = 'y'; break;
				case 0x0FF5A:
					ia5_0 = 'z'; break;
				case 0x0FF40:
					ia5_0 = '\''; break;
			}

			if (ia5_0 != 0) break;

			//
			// Punctuation
			//
			switch (ucp) {
				case 0x000A1: case 0x000BF:
					return 0;
				case 0x00060: case 0x000B4: case 0x02018: case 0x02019: case 0x0201A: case 0x0201B: case 0x02032: case 0x02035:
				case 0x0A78B: case 0x0A78C: case 0x0FF07: case 0x0275B: case 0x0275C: case 0x0275F:
					ia5_0 = '\''; break;
				case 0x000AD: case 0x000B7: case 0x02010: case 0x02011: case 0x02012: case 0x02013: case 0x02014: case 0x02015:
				case 0x02043: case 0x0204C: case 0x0204D: case 0x0207B: case 0x0208A: case 0x0208B: case 0x02212: case 0x02296:
				case 0x0229D: case 0x0229F: case 0x02A3A: case 0x01428: case 0x0FF0D: case 0x023AF: case 0x02796: case 0x0FE58:
				case 0x0FE63:
					ia5_0 = '-'; break;
				case 0x00021: case 0x00022: case 0x00023: case 0x00024: case 0x00025: case 0x00026: case 0x00027: case 0x00028:
				case 0x00029: case 0x0002A: case 0x0002B: case 0x0002C: case 0x0002D: case 0x0002E: case 0x0002F: case 0x0003A:
				case 0x0003B: case 0x0003C: case 0x0003D: case 0x0003E: case 0x0003F: case 0x00040: case 0x0005B: case 0x0005C:
				case 0x0005D: case 0x0005E: case 0x0005F: case 0x0007B: case 0x0007C: case 0x0007D: case 0x0007E:
					continue;
				case 0x000AF: case 0x02017: case 0x0203E: case 0x0203F: case 0x02040: case 0x02050: case 0x02054: case 0x0FF3F:
					ia5_0 = '_'; break;
				case 0x001C3: case 0x0FF01: case 0x02755: case 0x02757: case 0x02E53: case 0x0FE15: case 0x0FE57:
					ia5_0 = '!'; break;
				case 0x02297: case 0x022A0: case 0x02A02: case 0x02A36: case 0x02A37:
					ia5_0 = 'x'; break;
				case 0x02114: case 0x0FF03: case 0x0FE5F:
					ia5_0 = '#'; break;
				case 0x0229C: case 0x030A0: case 0x0FF1D: case 0x02E40: case 0x0FE66:
					ia5_0 = '='; break;
				case 0x0204E: case 0x02217: case 0x0229B: case 0x029C6: case 0x0FF0A: case 0x02731: case 0x0FE61:
					ia5_0 = '*'; break;
				case 0x0207A: case 0x02295: case 0x0229E: case 0x02A01: case 0x02A39: case 0x01429: case 0x0FF0B: case 0x02795:
				case 0x0FE62:
					ia5_0 = '+'; break;
				case 0x02236: case 0x00703: case 0x0FF1A: case 0x0FE13: case 0x0FE55:
					ia5_0 = ':'; break;
				case 0x0204F: case 0x0061B: case 0x0FF1B: case 0x0FE14:
					ia5_0 = ';'; break;
				case 0x02044: case 0x02215: case 0x02216: case 0x02298: case 0x029C4: case 0x029F8: case 0x1F67C: case 0x0FF0F:
					ia5_0 = '/'; break;
				case 0x029B8: case 0x029C5: case 0x029F5: case 0x029F9: case 0x1F67D: case 0x0FF3C:
					ia5_0 = '\\'; break;
				case 0x0201C: case 0x0201D: case 0x0201E: case 0x0201F: case 0x0301D: case 0x0301E: case 0x0301F: case 0x0FF02:
				case 0x0275D: case 0x0275E: case 0x02760:
					ia5_0 = '\"'; break;
				case 0x1F670: case 0x1F671: case 0x1F672: case 0x1F673: case 0x1F674: case 0x1F675: case 0x0FF06: case 0x0FE60:
					ia5_0 = '&'; break;
				case 0x002DC: case 0x0223C: case 0x0223D: case 0x0FF5E:
					ia5_0 = '~'; break;
				case 0x02022: case 0x02023: case 0x02027: case 0x02219: case 0x02299: case 0x022A1: case 0x025E6: case 0x029BE:
				case 0x029BF: case 0x02A00: case 0x01427: case 0x030FB: case 0x0FF65: case 0x10101: case 0x1091F: case 0x10A50:
				case 0x10AF4: case 0x11049: case 0x02E31:
					ia5_0 = '·'; break;
				case 0x029BC: case 0x02A38: case 0x02E13: case 0x02797:
					ia5_0 = '÷'; break;
				case 0x02024: case 0x0FF0E: case 0x02396:
					ia5_0 = '.'; break;
				case 0x02025: case 0x00705:
					ia5_0 = '.'; ia5_1 = '.'; break;
				case 0x02026: case 0x022EF:
					ia5_0 = '.'; ia5_1 = '.'; ia5_2 = '.'; break;
				case 0x02033: case 0x02036:
					ia5_0 = '\''; ia5_1 = '\''; break;
				case 0x02034: case 0x02037:
					ia5_0 = '\''; ia5_1 = '\''; ia5_2 = '\''; break;
				case 0x0203C:
					ia5_0 = '!'; ia5_1 = '!'; break;
				case 0x0203D: case 0x02048:
					ia5_0 = '?'; ia5_1 = '!'; break;
				case 0x02047:
					ia5_0 = '?'; ia5_1 = '?'; break;
				case 0x02049:
					ia5_0 = '!'; ia5_1 = '?'; break;
				case 0x02254:
					ia5_0 = ':'; ia5_1 = '='; break;
				case 0x02A74:
					ia5_0 = ':'; ia5_1 = ':'; ia5_2 = '='; ia5_3 = '='; break;
				case 0x02255:
					ia5_0 = '='; ia5_1 = ':'; break;
				case 0x02A75:
					ia5_0 = '='; ia5_1 = '='; break;
				case 0x02A76:
					ia5_0 = '='; ia5_1 = '='; ia5_2 = '='; break;
				case 0x029FA:
					ia5_0 = '+'; ia5_1 = '+'; break;
				case 0x029FB:
					ia5_0 = '+'; ia5_1 = '+'; ia5_2 = '+'; break;
				case 0x1FBC4: case 0x0FF1F: case 0x02753: case 0x02754: case 0x02E54: case 0x0FE16: case 0x0FE56:
					ia5_0 = '?'; break;
				case 0x0066B: case 0x0FF0C: case 0x0FF64: case 0x0FE50: case 0x0FE68:
					ia5_0 = ','; break;
				case 0x0FF20: case 0x0FE6B:
					ia5_0 = '@'; break;
				case 0x01801:
					ia5_0 = '.'; ia5_1 = '.'; ia5_2 = '.'; ia5_3 = '.'; break;
			}

			if (ia5_0 != 0) break;

			//
			// Unknown --> No replacements
			//

		} while (false);

		final int seq =
			ia5_3 != 0 ? 4 :
			ia5_2 != 0 ? 3 :
			ia5_1 != 0 ? 2 :
			ia5_0 != 0 ? 1 : ucp;

		switch (axis) {
			case AXIS_LOWERCASE: {
				switch (seq) {
					default: return Character.toLowerCase(ucp);
					case 4: ia5_3 = Character.toLowerCase(ia5_3);
					case 3: ia5_2 = Character.toLowerCase(ia5_2);
					case 2: ia5_1 = Character.toLowerCase(ia5_1);
					case 1: ia5_0 = Character.toLowerCase(ia5_0);
				}
			} break;

			case AXIS_UPPERCASE: {
				switch (seq) {
					default: return Character.toUpperCase(ucp);
					case 4: ia5_3 = Character.toUpperCase(ia5_3);
					case 3: ia5_2 = Character.toUpperCase(ia5_2);
					case 2: ia5_1 = Character.toUpperCase(ia5_1);
					case 1: ia5_0 = Character.toUpperCase(ia5_0);
				}
			} break;
		}

		ia5[0] = ia5_0;
		ia5[1] = ia5_1;
		ia5[2] = ia5_2;
		ia5[3] = ia5_3;

		return seq;
	}

}
