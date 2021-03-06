package algorithms.l07tree;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static common.Utils.centered;
import static common.Utils.widthByLevelDepth;

public class BST<K extends Comparable<K>, V> {

  class Node {
    K key;
    V value;
    Node left;
    Node right;
    int n;

    public Node(K key, V value, int n) {
      this.key = key;
      this.value = value;
      this.n = n;
    }
  }

  Node root;

  int size() {
    return size(root);
  }

  int size(Node x) {
    if (x == null) return 0;
    return x.n;
  }

  public Optional<V> get(K key) {
    return get(root, key);
  }

  private Optional<V> get(Node x, K key) {
    if (x == null) return Optional.empty();
    int cmp = key.compareTo(x.key);
    if      (cmp < 0) return get(x.left, key);
    else if (cmp > 0) return get(x.right, key);
    else return Optional.of(x.value);
  }

  public void put(K key, V value) {
    root = put(root, key, value);
  }

  private Node put(Node x, K key, V value) {
    if (x == null) return new Node(key, value, 1);
    int cmp = key.compareTo(x.key);
    if      (cmp < 0) x.left = put(x.left, key, value);
    else if (cmp > 0) x.right = put(x.right, key, value);
    else x.value = value;
    x.n = size(x.left) + size(x.right) + 1;
    return x;
  }

  public void remove(K key) {
    root = remove(root, key);
  }

  private Node remove(Node x, K key) {
    // finish. not found
    if (x == null) return null;
    int cmp = key.compareTo(x.key);
    if      (cmp < 0) x.left = remove(x.left, key);
    else if (cmp > 0) x.right = remove(x.right, key);
    else {
      // left is empty. just pull-up right
      if (x.left == null) return x.right;
      // right is empty. just pull-up left
      if (x.right == null) return x.left;
      // both occupied, need more work.
      x = performNodeRemoval(x);
    }
    return x;
  }

  /**
   * take the reference of current node
   * and return the new reference
   * for sub-tree with deleted node
   */
  private Node performNodeRemoval(Node x) {
    // 1. save the left sub-tree. we will attach it in 4.3
    Node savedLeft = x.left;
    // 2. find the minimal in the right sub-tree.
    // It will be new instead of deleted
    Node newNode = findMinFrom(x.right);
    // 3. remove the minimal from the right sub-tree
    Node newRight = deleteMinAndPullUpFrom(x.right);
    // 4.2. attach new right
    newNode.right = newRight;
    // 4.3. attach saved left
    newNode.left = savedLeft;
    return newNode;
  }

  /**
   * find the minimal value from the given node
   * minimal node always has x.left == null
   * it means that there are NO items less than it
   */
  private Node findMinFrom(Node x) {
    return x.left == null ? x : findMinFrom(x.left);
  }

  /**
   * delete min and pull-up from the given node
   */
  private Node deleteMinAndPullUpFrom(Node x) {
    // left is empty. we found it. skip it. just pull-up right. return right sub-tree
    if (x.left == null) return x.right;
    // we didn't find. need go left to find it
    x.left = deleteMinAndPullUpFrom(x.left);
    return x;
  }

  private Node findMaxFrom(Node x) {
    return x.right == null ? x : findMaxFrom(x.right);
  }

  public Optional<K> min() {
    if (root == null) return Optional.empty();
    Node found = findMinFrom(root);
    if (found == null) return Optional.empty();
    return Optional.of(found.key);
  }

  public Optional<K> max() {
    if (root == null) return Optional.empty();
    Node found = findMaxFrom(root);
    if (found == null) return Optional.empty();
    return Optional.of(found.key);
  }

  public int height() {
    return height(root, 0);
  }

  private int height(Node x, int h) {
    if (x == null) return h;
    return Math.max(
        height(x.left, h+1),
        height(x.right, h+1)
    );
  }

  public int width() {
    int h = height();
    return h == 0 ? 0 : 1 << (h-1);
  }

  public List<K> keys_traverse_breadth_it() {
    LinkedList<K> outcome = new LinkedList<>();
    LinkedList<Node> process = new LinkedList<>();
    LinkedList<Node> next = new LinkedList<>();
    process.add(root);

    while (!process.isEmpty() && root!=null) {
      while (!process.isEmpty()) {
        Node node = process.pollFirst();
        outcome.add(node.key);
        if (node.left != null) next.add(node.left);
        if (node.right != null) next.add(node.right);
      }
      process.addAll(next);
      next.clear();
    }

    return outcome;
  }

  public List<K> keys_traverse_breadth() {
    LinkedList<K> keys = new LinkedList<>();
    if (root!=null) keys_traverse_breadth(new LinkedList<Node>(){{ add(root); }}, keys);
    return keys;
  }

  private void keys_traverse_breadth(LinkedList<Node> process, LinkedList<K> acc) {
    if (process.isEmpty()) return;
    LinkedList<Node> next = new LinkedList<>();
    process.forEach(node -> {
      acc.add(node.key);
      if (node.left != null) next.add(node.left);
      if (node.right != null) next.add(node.right);
    });
    keys_traverse_breadth(next, acc);
  }

  public Collection<K> keys() {
    List<K> keys = new LinkedList<>();
    keys_add_all_to_acc(root, keys);
    return keys;
  }

  private void keys_add_all_to_acc(Node x, Collection<K> acc) {
    if (x == null) return;
    acc.add(x.key);
    keys_add_all_to_acc(x.left, acc);
    keys_add_all_to_acc(x.right, acc);
  }

  public List<K> keys_traverse_depth() {
    return keys_traverse_depth(root);
  }

  private List<K> keys_traverse_depth(Node x) {
    return (x == null) ? Collections.emptyList() : new LinkedList<K>() {{
      add(x.key);
      addAll(keys_traverse_depth(x.left));
      addAll(keys_traverse_depth(x.right));
    }};
  }

  class SList {
    final int level;
    final List<K> keys;

    SList(int level, List<K> keys) {
      this.level = level;
      this.keys = keys;
    }
  }

  private List<SList> empty() {
    /**
     * 1.        R
     * 2.    M       N
     * 3.  I   J   K   L
     * 4. A B C D E F G H
     */
    return IntStream.rangeClosed(1, height()) // iterate over levels 1..4
        .mapToObj(lv ->
            new SList(lv, IntStream.rangeClosed(1, 0b1 << (lv - 1)).mapToObj(n -> (K)null).collect(Collectors.toList()))
        ).collect(Collectors.toList());
  }

  private void traverseAndFill(Node x, int level, int pos, List<SList> acc) {
    if (x == null) return;
    acc.get(level).keys.set(pos, x.key);
    traverseAndFill(x.left, level+1, pos*2, acc);
    traverseAndFill(x.right, level+1, pos*2+1, acc);
  }

  private List<List<String>> represent() {
    final int SIZE = 4;
    final int DEPTH = height();
    List<SList> rep = empty();
    traverseAndFill(root, 0, 0, rep);
    return rep.stream().map(line ->
        line.keys.stream().map(key ->
            centered(key != null ? key.toString(): "", widthByLevelDepth(line.level, DEPTH, SIZE))
        ).collect(Collectors.toList()) // we remap keys to Strings
    ).collect(Collectors.toList());    // we have List<List<String>>
  }

  public String show() {
    final String NL = "\n\n";
    return represent().stream()
        .map(line -> String.join("", line))
        .collect(Collectors.joining(NL));
  }

}
