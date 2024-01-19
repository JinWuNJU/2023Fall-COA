package cpu.alu;

import org.junit.Test;
import util.DataType;

import static org.junit.Assert.assertEquals;

public class ALUMulTest {

	private final ALU alu = new ALU();
	private DataType src;
	private DataType dest;
	private DataType result;

	@Test
	public void MulTest1() {
		src = new DataType("11111111111111111111111111111010");
		dest = new DataType("11111111111111111111111111111001");
		result = alu.mul(src, dest);
		assertEquals("00000000000000000000000000101010", result.toString());
	}

}
