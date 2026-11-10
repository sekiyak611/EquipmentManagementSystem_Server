package jp.co.highjunk.equipment.auth.model;

import java.time.LocalDateTime;

/**
 * users の1行のうち、ログインに使う列（docs/データ設計.md 5.1）。
 * MyBatis が、列名（password_hash）を名前（passwordHash）に合わせて値を入れる（common.md 4章）。
 */
public class User {

	private long id;

	private String name;

	/** メールアドレス（小文字にそろえて保存している） */
	private String email;

	/** パスワードを bcrypt で変換した値。まだ決めていなければ null */
	private String passwordHash;

	private long roleId;

	/** ロックした日時。null ならロックされていない */
	private LocalDateTime lockedAt;

	/** この日時より前にログインしたセッションは、ログアウトさせる（docs/API設計.md 2.2） */
	private LocalDateTime sessionValidAfter;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public LocalDateTime getLockedAt() {
		return lockedAt;
	}

	public void setLockedAt(LocalDateTime lockedAt) {
		this.lockedAt = lockedAt;
	}

	public LocalDateTime getSessionValidAfter() {
		return sessionValidAfter;
	}

	public void setSessionValidAfter(LocalDateTime sessionValidAfter) {
		this.sessionValidAfter = sessionValidAfter;
	}

	/** パスワードの値をログなどに出さないよう、文字にするときは ID だけにする */
	@Override
	public String toString() {
		return "User[id=" + id + "]";
	}
}
