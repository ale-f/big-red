package dk.itu.big_red.utilities.io;

public class LineReadStrategy extends TotalReadStrategy {
	@Override
	protected boolean shouldBreak(byte b) {
		return (b == '\n');
	}
}
