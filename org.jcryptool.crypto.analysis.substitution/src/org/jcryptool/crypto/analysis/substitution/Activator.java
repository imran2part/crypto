package org.jcryptool.crypto.analysis.substitution;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jcryptool.core.logging.utils.LogUtil;
import org.jcryptool.crypto.analysis.substitution.calc.DynamicPredefinedStatisticsProvider;
import org.jcryptool.crypto.analysis.substitution.calc.TextStatistic;
import org.jcryptool.crypto.analysis.substitution.ui.modules.utils.PredefinedStatisticsProvider;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jcryptool.crypto.analysis.substitution"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	
	private static PredefinedStatisticsProvider statisticsProvider = null;
	
	public static PredefinedStatisticsProvider getPredefinedStatisticsProvider() {
		if(statisticsProvider == null) {
			try {
				statisticsProvider = new DynamicPredefinedStatisticsProvider();
			} catch (Exception e) {
				LogUtil.logError(PLUGIN_ID, e);
				return new PredefinedStatisticsProvider() {
					
					@Override
					public List<TextStatistic> getPredefinedStatistics() {
						return Collections.EMPTY_LIST;
					}
				};
			}
		}
		return statisticsProvider;
		
	}
}
