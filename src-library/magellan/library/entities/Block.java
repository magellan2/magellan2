package magellan.library.entities;

public interface Block {
  /**
   * Adds a Block to the current block
   */
  public Block newBlock(String name);

  public Block newBlock(String name, String id);

  /**
   * Adds an attribute to the block, possibly referenced by a name
   */
  public boolean addAttribute(String name, String value);

  public boolean addAttribute(String name, int value);

  public boolean addAttribute(String value);
}
