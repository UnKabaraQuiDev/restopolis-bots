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

	public TargetData() {
	}

	public TargetData(long id) {
		this.id = id;
	}

	public TargetData(TargetPlatform targetPlatform, List<DayOfWeek> days) {
		this.targetPlatform = targetPlatform;
		this.days = days;
	}

	public long getId() {
		return id;
	}

	public TargetPlatform getTargetPlatform() {
		return targetPlatform;
	}

	public List<DayOfWeek> getDays() {
		return days;
	}

	public void setDays(List<DayOfWeek> days) {
		this.days = days;
	}

	@Override
	public String toString() {
		return "TargetData@" + System.identityHashCode(this) + " [id=" + id + ", targetPlatform=" + targetPlatform
				+ ", days=" + days + "]";
	}

}
