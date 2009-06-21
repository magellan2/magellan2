package magellan.test.merge;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import magellan.library.GameData;
import magellan.library.io.cr.CRWriter;
import magellan.library.io.file.FileType;
import magellan.library.io.file.FileTypeFactory;
import magellan.test.GameDataBuilder;

import org.junit.Test;

public class WriteGameData{

	@Test
	public void testWriteCR() throws Exception {
		GameData data = new GameDataBuilder().createSimpleGameData();

		String fName = data.getDate().getDate()+"_testWriteCR.cr";
		File file = new File(WriteGameData.FILE_PREFIX + fName);
		
		WriteGameData.writeCR(data, file);
		assertTrue(file.exists());
	
		// TODO more tests
		file.delete();
	}

	public final static String FILE_PREFIX="test/junit";

	public static void writeCR(GameData data, File file) throws IOException {
		
		System.out.println("Writing file "+file.getAbsolutePath());
		FileType ft = FileTypeFactory.singleton().createFileType(file, false);
		ft.setCreateBackup(false);
		CRWriter crw = new CRWriter(null,ft,data.getEncoding());
		crw.write(data);
		crw.close();
	}
}
