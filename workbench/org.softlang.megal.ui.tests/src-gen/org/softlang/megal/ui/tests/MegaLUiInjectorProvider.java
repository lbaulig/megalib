/*
 * generated by Xtext 2.12.0
 */
package org.softlang.megal.ui.tests;

import com.google.inject.Injector;
import org.eclipse.xtext.testing.IInjectorProvider;
import org.softlang.megal.ui.internal.MegalActivator;

public class MegaLUiInjectorProvider implements IInjectorProvider {

	@Override
	public Injector getInjector() {
		return MegalActivator.getInstance().getInjector("org.softlang.megal.MegaL");
	}

}
