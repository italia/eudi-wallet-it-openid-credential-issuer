package it.ipzs.qeaaissuer.util;

import java.io.File;
import java.io.FileWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeyStoreUtil {

	private KeyStoreUtil() {

	}

	public static void storeKey(String path, String key) {

		if (!new File(path).exists()) {
			try (FileWriter fw = new FileWriter(path)) {
				fw.write(key);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

}
