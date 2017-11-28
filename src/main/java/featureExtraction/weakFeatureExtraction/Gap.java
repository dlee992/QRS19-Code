package featureExtraction.weakFeatureExtraction;

import entity.Cluster;

public class Gap {
	private int horizontalGap = 0;
	private int verticalGap = 0;
	private Cluster cluster = null;

	public Gap(int horizontalGap, int verticalGap, Cluster cluster) {
		this.horizontalGap = horizontalGap;
		this.verticalGap = verticalGap;
		this.cluster = cluster;
	}
	
	@Override
	public String toString() {
		return "Gap [horizontalGap=" + horizontalGap + ", verticalGap="
				+ verticalGap + ", cluster=" + cluster + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cluster == null) ? 0 : cluster.hashCode());
		result = prime * result + horizontalGap;
		result = prime * result + verticalGap;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Gap other = (Gap) obj;
		if (cluster == null) {
			if (other.cluster != null)
				return false;
		} else if (!cluster.equals(other.cluster))
			return false;
		if (horizontalGap != other.horizontalGap)
			return false;
		if (verticalGap != other.verticalGap)
			return false;
		return true;
	}
}
