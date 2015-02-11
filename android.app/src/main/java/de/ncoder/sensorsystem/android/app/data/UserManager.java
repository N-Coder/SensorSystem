/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Niko Fink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.ncoder.sensorsystem.android.app.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.security.auth.login.LoginException;

import de.ncoder.sensorsystem.events.event.SimpleEvent;
import de.ncoder.sensorsystem.manager.DataManager;
import de.ncoder.typedmap.Key;

public class UserManager extends DataManager {
	public static final Key<UserManager> KEY = new Key<>(UserManager.class);

	private String currentUserName;
	private boolean isLoggedIn;

	public String getUserName() {
		return currentUserName;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	// ------------------------------------------------------------------------

	public Future<UserManager> login(final String userName, char[] password) {
		return execute(new LoginTask(userName, password));
	}

	private class LoginTask implements Callable<UserManager> {
		private final String userName;
		private final char[] password;

		private LoginTask(String userName, char[] password) {
			this.userName = userName;
			this.password = password;
		}

		@Override
		public UserManager call() throws Exception {
			try {
				performChecks();
				doLogin();
				updateData();
				return UserManager.this;
			} finally {
				if (password != null) {
					Arrays.fill(password, (char) 0);
				}
			}
		}

		private void performChecks() throws LoginException {
			if (isLoggedIn()) {
				throw new LoginException("Already logged in as " + UserManager.this.currentUserName);
			}
			if (userName == null) {
				throw new NullPointerException("userName");
			}
			if (password == null) {
				throw new NullPointerException("password");
			}
			if (userName.isEmpty()) {
				throw new LoginException("Illegal user name");
			}
		}

		private void doLogin() throws InterruptedException, IOException {
			//Simulate some network action
			synchronized (this) {
				((Object) this).wait(10000);
			}
			if (userName.equals("io")) {
				throw new IOException("dummy");
			}
		}

		private void updateData() {
			currentUserName = userName;
			isLoggedIn = true;
			publish(new UserStatusChangedEvent());
		}
	}

	public Future<UserManager> logout() {
		return execute(new LogoutTask());
	}

	private class LogoutTask implements Callable<UserManager> {
		@Override
		public UserManager call() throws Exception {
			if (!isLoggedIn()) {
				return UserManager.this;
			}
			doLogout();
			updateData();
			return UserManager.this;
		}

		private void doLogout() throws InterruptedException, IOException {
			//Simulate some network action
			synchronized (this) {
				((Object) this).wait(10000);
			}
		}

		private void updateData() {
			currentUserName = null;
			isLoggedIn = false;
			publish(new UserStatusChangedEvent());
		}
	}

	// ------------------------------------------------------------------------

	public class UserStatusChangedEvent extends SimpleEvent {
		private final boolean isLoggedIn;
		private final String userName;

		private UserStatusChangedEvent() {
			this(UserManager.this.isLoggedIn, UserManager.this.currentUserName);
		}

		private UserStatusChangedEvent(boolean isLoggedIn, String userName) {
			super(null, getKey());
			this.isLoggedIn = isLoggedIn;
			this.userName = userName;
		}

		public boolean isLoggedIn() {
			return isLoggedIn;
		}

		public String getUserName() {
			return userName;
		}
	}
}
