

public class Utility {
	public boolean NotEqual(int a, int b) {
	return (a != b) ? true : false;
	}
	public boolean Equal(int a, int b) {
		return (a == b) ? true : false;
	}
	public boolean lessThan(int a, int b) {
		return (a < b) ? true : false;
	}
	public boolean greaterThan(int a, int b) {
		return (a > b) ? true : false;
	}
	public boolean oneLessThan(int a, int b) {
		return (a - b == -1) ? true : false;
	}
	public boolean oneGreaterThan(int a, int b) {
		return (a - b == 1) ? true : false;
	}
	public boolean oneLessOrGreaterThan(int a, int b) {
		return (a - b == -1 || a - b == 1) ? true : false;
	}
	
}
