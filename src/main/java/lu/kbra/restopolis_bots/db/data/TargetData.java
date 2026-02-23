package lu.kbra.restopolis_bots.db.data;

import java.time.DayOfWeek;
import java.util.List;

import lu.kbra.pclib.db.autobuild.column.AutoIncrement;
import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.impl.DataBaseEntry;
import lu.kbra.restopolis_bots.data.TargetPlatform;

public class TargetData implements DataBaseEntry {

	@PrimaryKey
	@Column
	@AutoIncrement
	protected long id;

	@Column(length = 32)
	protected TargetPlatform targetPlatform;
	
	@Column
	protected List<DayOfWeek> days;

}
