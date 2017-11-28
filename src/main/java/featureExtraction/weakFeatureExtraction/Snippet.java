package featureExtraction.weakFeatureExtraction;

public class Snippet {
    public int left;
    public int up;
    public int right;
    public int bottom;

	Snippet() {}

	Snippet(int left, int up, int right, int bottom) {
		this.left = left;
		this.up = up;
		this.right = right;
		this.bottom = bottom;
	}
	
	@Override
	public String toString() {
		return "Data Region: " + "[" + up + ", " + left + "] --> [" + bottom
				+ ", " + right + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Snippet)) {
			return false;
		}
		Snippet snippet = (Snippet) obj;

		return !(left != snippet.left || up != snippet.up || right != snippet.right
				|| bottom != snippet.bottom);
	}
}
