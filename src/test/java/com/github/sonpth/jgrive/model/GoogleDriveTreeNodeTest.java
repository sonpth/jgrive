package com.github.sonpth.jgrive.model;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.sonpth.jgrive.service.DriveFactory;
import com.google.api.services.drive.Drive;

public class GoogleDriveTreeNodeTest {
	private Drive drive;
	
	@BeforeEach
	public void setUp() throws IOException {
		drive = new DriveFactory(false).getInstance();
	}
	@Test
	public void testGetInstance() {
		GoogleDriveTreeNode harness = GoogleDriveTreeNode.getInstance(drive);
		boolean toggleFlag = true;
		for (TreeNode node: harness.getChildren()) {
			System.out.println(node.getName());

			if (toggleFlag && node.isFolder()) {
				toggleFlag = false;
				for(TreeNode subnode: node.getChildren()) {
					System.out.println("\t" + subnode.getName());
				}
			}
		}
	}
}
