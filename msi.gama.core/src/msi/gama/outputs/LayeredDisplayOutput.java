/*
 * GAMA - V1.4 http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gama.outputs;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import msi.gama.common.GamaPreferences;
import msi.gama.common.interfaces.*;
import msi.gama.common.util.GuiUtils;
import msi.gama.metamodel.shape.*;
import msi.gama.outputs.LayeredDisplayOutput.InfoValidator;
import msi.gama.outputs.layers.*;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.GamlAnnotations.validator;
import msi.gama.precompiler.*;
import msi.gama.runtime.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gaml.compilation.*;
import msi.gaml.descriptions.*;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.types.*;
import com.vividsolutions.jts.geom.Envelope;

/**
 * The Class LayerDisplayOutput.
 * 
 * @author drogoul
 */
@symbol(name = { IKeyword.DISPLAY }, kind = ISymbolKind.OUTPUT, with_sequence = true)
@facets(value = {
	@facet(name = IKeyword.BACKGROUND, type = IType.COLOR, optional = true, doc = @doc("Allows to fill the background of the display with a specific color")),
	@facet(name = IKeyword.NAME, type = IType.LABEL, optional = false),
	// WARNING VALIDER EN VERIFIANT LE TYPE DU DISPLAY
	@facet(name = IKeyword.TYPE, type = IType.LABEL, optional = true, doc = @doc("Allows to use either Java2D (for planar models) or OpenGL (for 3D models) as the rendering subsystem")),
	@facet(name = IKeyword.REFRESH_EVERY, type = IType.INT, optional = true, doc = @doc("Allows to refresh the display every n time steps (default is 1)")),
	@facet(name = IKeyword.TESSELATION, type = IType.BOOL, optional = true, doc = @doc("")),
	@facet(name = IKeyword.ZFIGHTING, type = IType.BOOL, optional = true, doc = @doc("Allows to alleviate a problem where agents at the same z would overlap each other in random ways")),
	@facet(name = IKeyword.TRACE, type = { IType.BOOL, IType.INT }, optional = true, doc = @doc("Allows to aggregate the visualization of agents at each timestep on the display. Default is false. If set to an int value, only the last n-th steps will be visualized. If set to true, no limit of timesteps is applied. ")),
	@facet(name = IKeyword.SCALE, type = { IType.BOOL, IType.FLOAT }, optional = true, doc = @doc("Allows to display a scale bar in the overlay. Accepts true/false or an unit name")),
	@facet(name = IKeyword.SHOWFPS, type = IType.BOOL, optional = true, doc = @doc("Allows to enable/disable the drawing of the number of frames per second")),
	@facet(name = IKeyword.DRAWENV, type = IType.BOOL, optional = true, doc = @doc("Allows to enable/disable the drawing of the world shape and the ordinate axes. Default can be configured in Preferences")),
	@facet(name = IKeyword.AMBIENT_LIGHT, type = { IType.INT, IType.COLOR }, optional = true),
	@facet(name = IKeyword.DIFFUSE_LIGHT, type = { IType.INT, IType.COLOR }, optional = true),
	@facet(name = IKeyword.CAMERA_POS, type = { IType.POINT, IType.AGENT }, optional = true),
	@facet(name = IKeyword.CAMERA_LOOK_POS, type = IType.POINT, optional = true),
	@facet(name = IKeyword.CAMERA_UP_VECTOR, type = IType.POINT, optional = true),
	@facet(name = IKeyword.POLYGONMODE, type = IType.BOOL, optional = true),
	@facet(name = IKeyword.AUTOSAVE, type = { IType.BOOL, IType.POINT }, optional = true, doc = @doc("Allows to save this display on disk. A value of true/false will save it at a resolution of 500x500. A point can be passed to personalize these dimensions")),
	@facet(name = IKeyword.OUTPUT3D, type = { IType.BOOL, IType.POINT }, optional = true) }, omissible = IKeyword.NAME)
@inside(symbols = { IKeyword.OUTPUT, IKeyword.PERMANENT })
@validator(InfoValidator.class)
public class LayeredDisplayOutput extends AbstractDisplayOutput {

	public static class InfoValidator implements IDescriptionValidator {

		/**
		 * Method validate()
		 * @see msi.gaml.compilation.IDescriptionValidator#validate(msi.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription d) {
			IExpressionDescription auto = d.getFacets().get(IKeyword.AUTOSAVE);
			if ( auto != null && auto.getExpression().isConst() &&
				auto.getExpression().literalValue().equals(IKeyword.TRUE) ) {
				d.info(
					"With autosave enabled, GAMA must remain the frontmost window and the display must not be covered or obscured by other windows",
					IGamlIssue.GENERAL, auto.getTarget(), IKeyword.AUTOSAVE);
			}
			// Are we in OpenGL world ?
			IExpressionDescription type = d.getFacets().get(IKeyword.TYPE);
			Boolean isOpenGLDefault = GamaPreferences.CORE_DISPLAY.getValue().equals("OpenGL");
			Boolean isOpenGLWanted =
				type == null ? isOpenGLDefault : type.getExpression().literalValue()
					.equals(LayeredDisplayOutput.OPENGL);

			if ( !isOpenGLWanted ) { return; }

			// Do we display a grid ?

			Boolean gridDisplayed = false;
			for ( IDescription desc : d.getChildren() ) {
				if ( desc.getKeyword().equals(IKeyword.GRID_POPULATION) ) {
					gridDisplayed = true;
					break;
				}
			}
			if ( !gridDisplayed ) { return; }
			IExpressionDescription zfight = d.getFacets().get(IKeyword.ZFIGHTING);
			Boolean zFightDefault = GamaPreferences.CORE_Z_FIGHTING.getValue();
			Boolean zFightWanted =
				zfight == null ? zFightDefault : zfight.getExpression().literalValue().equals(IKeyword.TRUE);
			if ( zFightWanted ) {
				String prefs = zFightDefault ? "(enabled by default in the Preferences)" : "";
				d.info("z_fighting " + prefs +
					" improves the rendering, but disables the selection of a single cell in a grid layer",
					IGamlIssue.GENERAL, zfight == null ? null : zfight.getTarget(), IKeyword.AUTOSAVE);
			}
		}
	}

	public static final String JAVA2D = "java2D";
	public static final String OPENGL = "opengl";
	public static final String WEB = "web";
	public static final String THREED = "3D";
	// public static final String SWT = "swt";

	private List<AbstractLayerStatement> layers;
	private Color backgroundColor = GamaPreferences.CORE_BACKGROUND.getValue();
	protected IDisplaySurface surface;
	String snapshotFileName;
	private boolean autosave = false;
	private boolean output3D = false;
	private boolean tesselation = true;
	private int traceDisplay = 0;
	private boolean z_fighting = GamaPreferences.CORE_Z_FIGHTING.getValue();
	private boolean draw_norm = GamaPreferences.CORE_DRAW_NORM.getValue();
	private boolean displayScale = GamaPreferences.CORE_SCALE.getValue();
	private boolean showfps = GamaPreferences.CORE_SHOW_FPS.getValue();
	private boolean drawEnv = GamaPreferences.CORE_DRAW_ENV.getValue();
	private Color ambientLightColor = new GamaColor(125, 125, 125, 255);
	private Color diffuseLightColor = new GamaColor(125, 125, 125, 255);
	// Set it to (-1,-1,-1) to set the camera with the right value if no value defined.
	private ILocation cameraPos = new GamaPoint(-1, -1, -1);
	private ILocation cameraLookPos = new GamaPoint(-1, -1, -1);
	private ILocation cameraUpVector = new GamaPoint(0, 1, 0);
	private boolean constantAmbientLight = true;
	private boolean constantDiffuseLight = true;
	private boolean constantCamera = true;
	private boolean constantCameraLook = true;
	private boolean polygonMode = true;
	private String displayType = GamaPreferences.CORE_DISPLAY.getValue().equalsIgnoreCase(JAVA2D) ? JAVA2D : OPENGL;
	private ILocation imageDimension = new GamaPoint(-1, -1);
	private ILocation output3DNbCycles = new GamaPoint(0, 0);
	private double envWidth;
	private double envHeight;

	public LayeredDisplayOutput(final IDescription desc) {
		super(desc);

		if ( hasFacet(IKeyword.TYPE) ) {
			displayType = getLiteral(IKeyword.TYPE);
		}
		layers = new GamaList<AbstractLayerStatement>();

	}

	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		boolean result = super.init(scope);
		if ( !result ) { return false; }
		final IExpression color = getFacet(IKeyword.BACKGROUND);
		if ( color != null ) {
			setBackgroundColor(Cast.asColor(getScope(), color.value(getScope())));
		}

		final IExpression auto = getFacet(IKeyword.AUTOSAVE);
		if ( auto != null ) {
			if ( auto.getType().equals(Types.get(IType.POINT)) ) {
				autosave = true;
				imageDimension = Cast.asPoint(getScope(), auto.value(getScope()));
			} else {
				autosave = Cast.asBool(getScope(), auto.value(getScope()));
			}
		}
		for ( final ILayerStatement layer : getLayers() ) {
			// try {
			layer.setDisplayOutput(this);
			if ( !getScope().init(layer) ) { return false; }
			// } catch (final GamaRuntimeException e) {
			// GAMA.reportError(e, true);
			// return false;
			// }
		}

		// OpenGL parameter initialization
		final IExpression tess = getFacet(IKeyword.TESSELATION);
		if ( tess != null ) {
			setTesselation(Cast.asBool(getScope(), tess.value(getScope())));
		}

		final IExpression z = getFacet(IKeyword.ZFIGHTING);
		if ( z != null ) {
			setZFighting(Cast.asBool(getScope(), z.value(getScope())));
		}

		final IExpression scale = getFacet(IKeyword.SCALE);
		if ( scale != null ) {
			if ( scale.getType().equals(Types.get(IType.BOOL)) ) {
				displayScale = Cast.asBool(getScope(), scale.value(getScope()));
			} else {
				displayScale = true;
			}
		}

		final IExpression fps = getFacet(IKeyword.SHOWFPS);
		if ( fps != null ) {
			setShowFPS(Cast.asBool(getScope(), fps.value(getScope())));
		}

		computeTrace(getScope());

		final IExpression denv = getFacet(IKeyword.DRAWENV);
		if ( denv != null ) {
			setDrawEnv(Cast.asBool(getScope(), denv.value(getScope())));
		}

		final IExpression light = getFacet(IKeyword.AMBIENT_LIGHT);
		if ( light != null ) {

			if ( light.getType().equals(Types.get(IType.COLOR)) ) {
				setAmbientLightColor(Cast.asColor(getScope(), light.value(getScope())));
			} else {
				final int meanValue = Cast.asInt(getScope(), light.value(getScope()));
				setAmbientLightColor(new GamaColor(meanValue, meanValue, meanValue, 255));
			}

			if ( light.isConst() ) {
				constantAmbientLight = true;
			} else {
				constantAmbientLight = false;
			}

		}

		final IExpression light2 = getFacet(IKeyword.DIFFUSE_LIGHT);
		if ( light2 != null ) {

			if ( light2.getType().equals(Types.get(IType.COLOR)) ) {
				setDiffuseLightColor(Cast.asColor(getScope(), light2.value(getScope())));
			} else {
				final int meanValue = Cast.asInt(getScope(), light2.value(getScope()));
				setDiffuseLightColor(new GamaColor(meanValue, meanValue, meanValue, 255));
			}

			if ( light2.isConst() ) {
				constantDiffuseLight = true;
			} else {
				constantDiffuseLight = false;
			}

		}

		final IExpression camera = getFacet(IKeyword.CAMERA_POS);
		if ( camera != null ) {

			setCameraPos(Cast.asPoint(getScope(), camera.value(getScope())));

			if ( camera.isConst() ) {
				constantCamera = true;
			} else {
				constantCamera = false;
			}

		}

		final IExpression cameraLook = getFacet(IKeyword.CAMERA_LOOK_POS);
		if ( cameraLook != null ) {
			setCameraLookPos(Cast.asPoint(getScope(), cameraLook.value(getScope())));

			if ( cameraLook.isConst() ) {
				constantCameraLook = true;
			} else {
				constantCameraLook = false;
			}

		}
		// Set the up vector of the opengl Camera (see gluPerspective)
		final IExpression cameraUp = getFacet(IKeyword.CAMERA_UP_VECTOR);
		if ( cameraUp != null ) {
			setCameraUpVector(Cast.asPoint(getScope(), cameraUp.value(getScope())));
		}

		final IExpression poly = getFacet(IKeyword.POLYGONMODE);
		if ( poly != null ) {
			setPolygonMode(Cast.asBool(getScope(), poly.value(getScope())));
		}

		final IExpression out3D = getFacet(IKeyword.OUTPUT3D);
		if ( out3D != null ) {
			if ( out3D.getType().equals(Types.get(IType.POINT)) ) {
				setOutput3D(true);
				setOutput3DNbCycles(Cast.asPoint(getScope(), out3D.value(getScope())));
			} else {
				setOutput3D(Cast.asBool(getScope(), out3D.value(getScope())));
			}
		}
		Envelope env = scope.getSimulationScope().getEnvelope();
		this.envWidth = env.getWidth();
		this.envHeight = env.getHeight();
		createSurface(env);
		return true;
	}

	@Override
	public boolean step(final IScope scope) throws GamaRuntimeException {
		for ( final ILayerStatement layer : getLayers() ) {
			getScope().step(layer);
		}
		return true;
	}

	@Override
	public void update() throws GamaRuntimeException {
		if ( surface == null ) { return; }
		// /////////////// dynamic Lighting ///////////////////
		if ( !constantAmbientLight ) {
			final IExpression light = getFacet(IKeyword.AMBIENT_LIGHT);
			if ( light != null ) {
				if ( light.getType().equals(Types.get(IType.COLOR)) ) {
					setAmbientLightColor(Cast.asColor(getScope(), light.value(getScope())));
				} else {
					final int meanValue = Cast.asInt(getScope(), light.value(getScope()));
					setAmbientLightColor(new GamaColor(meanValue, meanValue, meanValue, 255));
				}
			}
		}

		if ( !constantDiffuseLight ) {
			final IExpression light2 = getFacet(IKeyword.DIFFUSE_LIGHT);
			if ( light2 != null ) {
				if ( light2.getType().equals(Types.get(IType.COLOR)) ) {
					setDiffuseLightColor(Cast.asColor(getScope(), light2.value(getScope())));
				} else {
					final int meanValue = Cast.asInt(getScope(), light2.value(getScope()));
					setDiffuseLightColor(new GamaColor(meanValue, meanValue, meanValue, 255));
				}
			}
		}

		// /////////////////// dynamic camera ///////////////////
		if ( !constantCamera ) {
			final IExpression camera = getFacet(IKeyword.CAMERA_POS);
			if ( camera != null ) {
				setCameraPos(Cast.asPoint(getScope(), camera.value(getScope())));
			}
			// graphics.setCameraPosition(getCameraPos());
		}

		if ( !constantCameraLook ) {
			final IExpression cameraLook = getFacet(IKeyword.CAMERA_LOOK_POS);
			if ( cameraLook != null ) {
				setCameraLookPos(Cast.asPoint(getScope(), cameraLook.value(getScope())));
			}
		}
		computeTrace(getScope());

		// GuiUtils.debug("LayeredDisplayOutput.update");
		surface.updateDisplay();

	}

	private void computeTrace(final IScope scope) {
		final IExpression agg = getFacet(IKeyword.TRACE);
		if ( agg != null ) {
			int limit = 0;
			if ( agg.getType().id() == IType.BOOL && Cast.asBool(getScope(), agg.value(getScope())) ) {
				limit = Integer.MAX_VALUE;
			} else {
				limit = Cast.asInt(getScope(), agg.value(getScope()));
			}
			setTraceDisplay(limit);
		}
	}

	@Override
	public void forceUpdate() throws GamaRuntimeException {
		// GUI.debug("Updating output " + getName());
		if ( surface != null /* && surface.canBeUpdated() */) {
			// GUI.debug("Updating the surface of output " + getName());
			surface.forceUpdateDisplay();
		}
	}

	//
	// @Override
	// public void schedule() throws GamaRuntimeException {
	// super.schedule();
	// getScope().step(this);
	// }

	public void setImageFileName(final String fileName) {
		snapshotFileName = fileName;
	}

	public boolean shouldDisplayScale() {
		return displayScale;
	}

	@Override
	public void dispose() {
		if ( disposed ) { return; }
		if ( surface != null ) {
			surface.setSynchronized(false);
		}
		super.dispose();
		if ( surface != null ) {
			surface.dispose();
		}
		surface = null;
		getLayers().clear();
	}

	protected void createSurface(final Envelope env) {
		if ( surface != null ) {
			surface.outputChanged(envWidth, envHeight, this);
			return;
		}
		surface = GuiUtils.getDisplaySurfaceFor(displayType, this, envWidth, envHeight);
		if ( !GuiUtils.isInHeadLessMode() ) {
			// FIXME These lines do nothing...
			surface.setSnapshotFileName(GAMA.getModel().getName() + "_display_" + getName());
			surface.setAutoSave(autosave, (int) imageDimension.getX(), (int) imageDimension.getY());
		}
	}

	public void setSurface(final IDisplaySurface sur) {
		surface = sur;
	}

	public double getEnvWidth() {
		return envWidth;
	}

	public double getEnvHeight() {
		return envHeight;
	}

	@Override
	public String getViewId() {
		if ( displayType.equals(WEB) ) { return GuiUtils.WEB_VIEW_ID; }
		return GuiUtils.LAYER_VIEW_ID;
	}

	public IDisplaySurface getSurface() {
		return surface;
	}

	@Override
	public List<? extends ISymbol> getChildren() {
		return getLayers();
	}

	@Override
	public void setChildren(final List<? extends ISymbol> commands) {
		setLayers((List<AbstractLayerStatement>) commands);
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public BufferedImage getImage() {
		return surface.getImage();
	}

	public void setBackgroundColor(final Color background) {
		this.backgroundColor = background;
		if ( surface != null ) {
			surface.setBackgroundColor(background);
		}
	}

	public void setLayers(final List<AbstractLayerStatement> layers) {
		this.layers = layers;
	}

	List<AbstractLayerStatement> getLayers() {
		return layers;
	}

	@Override
	public void pause() {
		super.pause();
		surface.setPaused(true);
	}

	@Override
	public void resume() {
		super.resume();
		surface.setPaused(false);
		// getScope().step(this);
	}

	public boolean isOpenGL() {
		return displayType.equals(OPENGL) || displayType.equals(THREED);
	}

	public boolean getTesselation() {
		return tesselation;
	}

	private void setTesselation(final boolean tesselation) {
		this.tesselation = tesselation;
	}

	public boolean getZFighting() {
		return z_fighting;
	}

	private void setZFighting(final boolean z) {
		this.z_fighting = z;
	}
	
	public boolean getDrawNorm() {
		return draw_norm;
	}

	private void setDrawNorm(final boolean draw_norm) {
		this.draw_norm = draw_norm;
	}

	public boolean getShowFPS() {
		return showfps;
	}

	private void setShowFPS(final boolean fps) {
		this.showfps = fps;
	}

	public int getTraceDisplay() {
		return traceDisplay;
	}

	private void setTraceDisplay(final int agg) {
		this.traceDisplay = agg;
	}

	public boolean getDrawEnv() {
		return drawEnv;
	}

	private void setDrawEnv(final boolean drawEnv) {
		this.drawEnv = drawEnv;
	}

	public boolean getOutput3D() {
		return output3D;
	}

	private void setOutput3D(final boolean output3D) {
		this.output3D = output3D;
	}

	public ILocation getCameraPos() {
		return cameraPos;
	}

	private void setCameraPos(final ILocation cameraPos) {
		this.cameraPos = cameraPos;
	}

	public ILocation getCameraLookPos() {
		return cameraLookPos;
	}

	private void setCameraLookPos(final ILocation cameraLookPos) {
		this.cameraLookPos = cameraLookPos;
	}

	public ILocation getCameraUpVector() {
		return cameraUpVector;
	}

	private void setCameraUpVector(final ILocation cameraUpVector) {
		this.cameraUpVector = cameraUpVector;
	}

	public Color getAmbientLightColor() {
		return ambientLightColor;
	}

	private void setAmbientLightColor(final Color ambientLightColor) {
		this.ambientLightColor = ambientLightColor;
	}

	public Color getDiffuseLightColor() {
		return diffuseLightColor;
	}

	private void setDiffuseLightColor(final Color diffuseLightColor) {
		this.diffuseLightColor = diffuseLightColor;
	}

	public boolean getPolygonMode() {
		return polygonMode;
	}

	private void setPolygonMode(final boolean polygonMode) {
		this.polygonMode = polygonMode;
	}

	public ILocation getOutput3DNbCycles() {
		return output3DNbCycles;
	}

	private void setOutput3DNbCycles(final ILocation output3DNbCycles) {
		this.output3DNbCycles = output3DNbCycles;
	}

}
