package platformer;

import defrac.display.DisplayObject;
import defrac.display.GLSurface;
import defrac.gl.GLFrameBuffer;
import defrac.gl.GLSubstrate;
import defrac.lang.Lists;
import platformer.gl.GLRenderer;
import platformer.renderer.Renderer;
import platformer.renderer.RendererContext;
import platformer.renderer.Sprite;
import platformer.tmx.MapData;
import platformer.tmx.TileSet;
import platformer.utils.TinyConsole;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Andre Michelle
 */
public final class Platformer implements RendererContext
{
	private final MapData mapData;
	private final int width;
	private final int height;

	private final int pixelWidth;
	private final int pixelHeight;

	private final GLRenderer glRenderer;

	private final List<Renderer> renderPipe;

	private long startTime;
	private int time;

	private int offsetX = 0;
	private int offsetY = 0;

	public Platformer( @Nonnull final MapData mapData, final int width, final int height )
	{
		this.mapData = mapData;
		this.width = width;
		this.height = height;

		pixelWidth = width * mapData.tileWidth;
		pixelHeight = height * mapData.tileHeight;

		glRenderer = new GLRenderer( 256 );

		renderPipe = Lists.newArrayList();

		startTime = System.currentTimeMillis();
	}

	@Override
	public int width()
	{
		return width;
	}

	@Override
	public int height()
	{
		return height;
	}

	@Override
	public int mapWidth()
	{
		return mapData.width;
	}

	@Override
	public int mapHeight()
	{
		return mapData.height;
	}

	@Override
	public int tileWidth()
	{
		return mapData.tileWidth;
	}

	@Override
	public int tileHeight()
	{
		return mapData.tileHeight;
	}

	@Override
	public int pixelWidth()
	{
		return pixelWidth;
	}

	@Override
	public int pixelHeight()
	{
		return pixelHeight;
	}

	@Override
	public int offsetX()
	{
		return offsetX;
	}

	@Override
	public int offsetY()
	{
		return offsetY;
	}

	@Nonnull
	@Override
	public TileSet[] tileSets()
	{
		return mapData.tileSets;
	}

	@Nonnull
	@Override
	public GLRenderer imageRenderer()
	{
		return glRenderer;
	}

	@Override
	public int currentTime()
	{
		return time;
	}

	public void addRenderer( @Nonnull final Renderer renderer )
	{
		renderPipe.add( renderer );
	}

	public void removeRenderer( @Nonnull final Renderer renderer )
	{
		renderPipe.remove( renderer );
	}

	/**
	 * Moves the view-port by the given x, y offset in pixels
	 *
	 * @param deltaX The offset for the x-coordinate
	 * @param deltaY The offset for the y-coordinate
	 * @return True, if the view-port has been moved (clamped by map-dimensions)
	 */
	public boolean moveBy( final int deltaX, final int deltaY )
	{
		return moveTo( offsetX + deltaX, offsetY + deltaY );
	}

	/**
	 * Moves the view-port to the given x, y position in pixels
	 *
	 * @param offsetX The value for the x-coordinate
	 * @param offsetY The value for the y-coordinate
	 * @return True, if the view-port has been moved (clamped by map-dimensions)
	 */
	public boolean moveTo( final int offsetX, final int offsetY )
	{
		final int newX = Math.max( 0, Math.min( offsetX, ( mapData.width - width ) * mapData.tileWidth ) );
		final int newY = Math.max( 0, Math.min( offsetY, ( mapData.height - height ) * mapData.tileHeight ) );

		if( this.offsetX == newX && this.offsetY == newY )
			return false;

		this.offsetX = newX;
		this.offsetY = newY;

		return true;
	}

	/**
	 * Centers a sprite.
	 *
	 * @param sprite The target sprite to be aligned to.
	 */
	public void center( @Nonnull final Sprite sprite )
	{
		moveTo( sprite.x() + ( sprite.width() - pixelWidth ) / 2, sprite.y() - pixelHeight / 2 );
	}

	/**
	 * Restarts the local time for animations
	 */
	public void restartTime()
	{
		startTime = System.currentTimeMillis();
	}

	/**
	 * Creates a displayObject for the displayList
	 *
	 * @return A DisplayObject
	 */
	@Nonnull
	public DisplayObject createDisplayObject()
	{
		return new GLSurface( pixelWidth, pixelHeight, ( surface1, gl, frameBuffer, renderBuffer, surfaceTexture, width, height, viewportWidth, viewportHeight, transparent ) -> {
			renderCycle( gl );
			TinyConsole.get().log( "Calls " + glRenderer.drawCalls, "Triangles " + glRenderer.drawTriangles );
		} );
	}

	/**
	 * Creates a ui.GLSurface.Renderer
	 *
	 * @return A ui.GLSurface.Renderer
	 */
	@Nonnull
	public defrac.ui.GLSurface.Renderer createGLSurfaceRenderer()
	{
		return new defrac.ui.GLSurface.Renderer()
		{
			@Override
			public void onSurfaceRender( @Nonnull final GLSubstrate glSubstrate )
			{
				renderCycle( glSubstrate );
			}

			@Override
			public void onSurfaceChanged( @Nonnull final GLSubstrate glSubstrate, final GLFrameBuffer glFrameBuffer, final int i, final int i1 )
			{
				// TODO Invalide GL programs
			}

			@Override
			public void onSurfaceCreated( @Nonnull final GLSubstrate glSubstrate, final GLFrameBuffer glFrameBuffer )
			{
				// TODO Invalide GL programs
			}
		};
	}

	private void renderCycle( @Nonnull final GLSubstrate glSubstrate )
	{
		time = ( int ) ( System.currentTimeMillis() - startTime );

		glRenderer.begin( glSubstrate, pixelWidth, pixelHeight, mapData.backgroundColor );

		for( final Renderer pipe : renderPipe )
			pipe.renderLayer();

		glRenderer.complete();
	}

	@Nonnull
	public MapData mapData()
	{
		return mapData;
	}

	@Override
	public String toString()
	{
		return "[Screen" +
				" width: " + width +
				", height: " + height +
				", pixelWidth: " + pixelWidth +
				", pixelHeight: " + pixelHeight +
				"]";
	}
}