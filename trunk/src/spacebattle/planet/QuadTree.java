package spacebattle.planet;

public interface QuadTree {
	// TODO: Needs a method for determining position, other then the local/absolute transform.
	public void setIndex(int index);
	public int getIndex();

	public boolean isLeaf();

}
