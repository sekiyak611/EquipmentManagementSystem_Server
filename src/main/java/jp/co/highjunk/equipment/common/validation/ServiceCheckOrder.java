package jp.co.highjunk.equipment.common.validation;

/**
 * ServiceInDto のチェックを確かめる順番（詳細設計 common.md。CLAUDE.md 5章）。
 * チェック用アノテーションの groups に付ける。ServiceInDtoChecker は、First → Second → 何も付けないもの（Default）の順に確かめ、
 * ある段で守れないものがあれば、そこで止めて、後ろの段は確かめない。
 * <ul>
 * <li>First：対象があるか（例：種別がある。守れなければ 404）</li>
 * <li>Second：対象の今の状態と合うか（例：version が同じ、使われていない。守れなければ 409 や 400）</li>
 * <li>Default：そのほか（例：名前が重ならない。入力欄のエラー 400）</li>
 * </ul>
 */
public final class ServiceCheckOrder {

	private ServiceCheckOrder() {
	}

	/** 1段目：対象があるか。 */
	public interface First {
	}

	/** 2段目：対象の今の状態と合うか。 */
	public interface Second {
	}
}
