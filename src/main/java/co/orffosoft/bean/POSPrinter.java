package co.orffosoft.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class POS {
	
	//Enum value code start region
	public enum Justifications {
		Left(0), Center(1), Right(2);

		public int GetValue() {
			return value;
		}

		private int value;

		private Justifications(int value) {
			this.value = value;
		}
	}



	public enum PrintStyle {
		None(0), FontB(1), Bold(1 << 3), DoubleHeight(1 << 4), DoubleWidth(1 << 5), Underline(1 << 7);

		private int value;

		public int GetValue() {
			return value;
		}

		private PrintStyle(int value) {
			this.value = value;
		}
	}

	public enum ErrorsCorrection {
		Percent7(48), // 0x00000030
		Percent15(49), // 0x00000031
		Percent25(50), // 0x00000032
		Percent30(51); // 0x00000033

		private int value;

		public int GetValue() {
			return value;
		}

		private ErrorsCorrection(int value) {
			this.value = value;
		}
	}




	//Enum value code end region
	
	//Commands code end region
	public static class Cmd {
		public static byte ESC = 0x1B;
		public static byte GS = 0x1D;
	}

	public static class Ops {
		public static byte Initialize = 0x40;
		public static byte EnableDisable = 0x3D;
		public static byte PaperCut = 0x56;
	}

	public static class Chars {
		public static byte StyleMode = 0x21;
		public static byte Alignment = 0x61;
		public static byte ReversePrintMode = 0x42;
		public static byte RightCharacterSpacing = 0x20;
		public static byte UpsideDownMode = 0x7B;
	}

	public static class Whitespace {
		// TODO: tabs?
		public static byte Linefeed = 0x0A;
		public static byte FeedLines = 0x64;
		public static byte FeedLinesReverse = 0x65;
		public static byte FeedDots = 0x4A;
		public static byte ResetLineSpacing = 0x32;
		public static byte LineSpacingInDots = 0x33;
	}

	public static class Status {
		public static byte AutomaticStatusBack = 0x61;
		public static byte AutomaticInkStatusBack = 0x6A;
	}

	public static class Functions {
		public static byte PaperCutFullCut = 0x00;
		public static byte PaperCutFullCutWithFeed = 0x41;
		public static byte PaperCutPartialCut = 0x01;
		public static byte PaperCutPartialCutWithFeed = 0x42;
	}

	public static class Barcodes {
		public static byte PrintBarcode = 0x6B;
		public static byte SetBarcodeHeightInDots = 0x68;
		public static byte SetBarWidth = 0x77;
		public static byte SetBarLabelPosition = 0x48;
		public static byte SetBarLabelFont = 0x66;
	}

	public static class Images {
		public static byte ImageCmdParen = 0x28;
		public static byte ImageCmd8 = 0x38;
		public static byte ImageCmdL = 0x4C;
	}
	//Commands code end region
	
	public static class POSPrinter {
		
		//Common Methods start region
		
		public static byte[] toByteArray(Enum<?> e) {
			return e.name().getBytes();
		}

		public static byte[] Justification(Justifications justification) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();

			bytes.write((byte) 27);
			bytes.write((byte) ('a'));
			bytes.write((byte) justification.GetValue());

			return bytes.toByteArray();
		}

		public static byte[] CutPage() {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			bytes.write((byte) ((char) (0x1D)));
			bytes.write((byte) ('V'));
			bytes.write((byte) 66);
			bytes.write((byte) 3);

			return bytes.toByteArray();
		}

		public static byte[] SetStyles(PrintStyle style) {
			return new byte[] { Cmd.ESC, Chars.StyleMode, (byte) style.GetValue() };
		}

		public static byte[] LeftAlign() {
			return new byte[] { Cmd.ESC, Chars.Alignment, (byte) Justifications.Left.GetValue() };
		}

		public static byte[] CenterAlign() {
			return new byte[] { Cmd.ESC, Chars.Alignment, (byte) Justifications.Center.GetValue() };
		}

		public static byte[] RightAlign() {
			return new byte[] { Cmd.ESC, Chars.Alignment, (byte) Justifications.Right.GetValue() };
		}

		public static byte[] RightCharacterSpacing(int spaceCount) {
			return new byte[] { Cmd.ESC, Chars.RightCharacterSpacing, (byte) spaceCount };
		}

		//Common Methods end region
		
		//Sub Classes for size, barcode, font and Qr code start region
		
		public static class CharSize {
			public static byte[] Normal() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 0 };
			}

			public static byte[] DoubleWidth2() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 16 };
			}

			public static byte[] DoubleWidth3() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 32 };
			}

			public static byte[] DoubleWidth4() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 48 };
			}

			public static byte[] DoubleWidth5() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 64 };
			}

			public static byte[] DoubleWidth6() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 80 };
			}

			public static byte[] DoubleWidth7() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 96 };
			}

			public static byte[] DoubleWidth8() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 112 };
			}

			public static byte[] DoubleHeight2() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 1 };
			}

			public static byte[] DoubleHeight3() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 2 };
			}

			public static byte[] DoubleHeight4() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 3 };
			}

			public static byte[] DoubleHeight5() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 4 };
			}

			public static byte[] DoubleHeight6() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 5 };
			}

			public static byte[] DoubleHeight7() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 6 };
			}

			public static byte[] DoubleHeight8() {
				return new byte[] { (byte) 29, (byte) '!', (byte) 7 };
			}
		}
		}

		public static class FontSelect {
			public static byte[] FontA() {
				return new byte[] { (byte) 27, (byte) 'M', (byte) 0 };
			}

			public static byte[] FontB() {
				return new byte[] { (byte) 27, (byte) 'M', (byte) 1 };
			}

			public static byte[] FontC() {
				return new byte[] { (byte) 27, (byte) 'M', (byte) 2 };
			}

			public static byte[] FontD() {
				return new byte[] { (byte) 27, (byte) 'M', (byte) 3 };
			}

			public static byte[] FontE() {
				return new byte[] { (byte) 27, (byte) 'M', (byte) 4 };
			}

			public static byte[] SpecialFontA() {
				return new byte[] { (byte) 27, (byte) 'M', (byte) 97 };
			}

			public static byte[] SpecialFontB() {
				return new byte[] { (byte) 27, (byte) 'M', (byte) 98 };
			}
		}


		
		//Sub Classes for size, barcode, font and Qr code end region
	}


