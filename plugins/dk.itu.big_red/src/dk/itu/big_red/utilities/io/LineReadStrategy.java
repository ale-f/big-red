package dk.itu.big_red.utilities.io;

public class LineReadStrategy extends DelimitedReadStrategy {
	@Override
	protected boolean shouldBreak(byte b) {
		return (b == '\n');
	}
}
