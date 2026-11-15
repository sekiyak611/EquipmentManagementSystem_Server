package jp.co.highjunk.equipment.api.auth.update.password.service;

/**
 * 現在のパスワードを間違えて、いまロックした（A-07）。返すものは、{@link AuthUpdatePasswordWrongCurrentException} と同じ
 * （ロックしたことは、画面に出さない。画面設計 2.1 の4）。Controller は、これを受け取ったら、この端末をログアウトさせる（画面設計 7章）。
 */
public class AuthUpdatePasswordAccountLockedException extends AuthUpdatePasswordWrongCurrentException {
}
