package com.github.sonpth.jgrive.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocalTreeNodeTest {
	@Test
	public void testGetInstance() {
		//Root
		LocalTreeNode harness = LocalTreeNode.getInstance(".");
		List<? extends TreeNode> children = harness.getChildren();
		assertFalse(children.isEmpty());
		Set<String> expected = new HashSet<>(Arrays.asList("src", "target", "README.md"));
		Set<String> actual = new HashSet<>();
		TreeNode srcFolder = null;
		for (TreeNode node: children) {
			actual.add(node.getName());
			if ("src".equals(node.getName())){
				srcFolder = node;
			}
		}
		assertTrue(actual.containsAll(expected));
		
		//A child
		actual = new HashSet<>();
		expected = new HashSet<>(Arrays.asList("main", "test"));
		for (TreeNode node: srcFolder.getChildren()) {
			actual.add(node.getName());
			if ("src".equals(node.getName())){
				srcFolder = node;
			}
		}
	}
}
