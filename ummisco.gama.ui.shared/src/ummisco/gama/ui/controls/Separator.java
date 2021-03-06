/*********************************************************************************************
 *
 * 'Separator.java, in plugin ummisco.gama.ui.shared, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gama.ui.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class Separator extends WorkbenchWindowControlContribution {

	public Separator() {}

	public Separator(final String id) {
		super(id);
	}

	@Override
	protected Control createControl(final Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setSize(18, 32);
		return label;
	}

	@Override
	protected int computeWidth(final Control control) {
		return control.computeSize(18, 32).x;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isGroupMarker() {
		return false;
	}

	@Override
	public boolean isSeparator() {
		return true;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

}
