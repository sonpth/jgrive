package com.github.sonpth.jgrive;

import java.io.IOException;

import com.github.sonpth.jgrive.service.DriveFactory;
import com.google.api.services.drive.Drive;

public class JGrive {
	public static void main(String[] args) throws IOException{
		Drive drive = new DriveFactory(false).getInstance();
	}
}
