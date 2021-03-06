/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */

package freemarker.template;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import freemarker.core.BugException;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapper.SettingAssignments;
import freemarker.ext.dom.NodeModel;

/**
 * The default implementation of the {@link ObjectWrapper} interface.
 */
public class DefaultObjectWrapper extends freemarker.ext.beans.BeansWrapper {
    
    /** @deprecated Use {@link #getInstance(Version)} instead, but mind its performance */
    static final DefaultObjectWrapper instance = new DefaultObjectWrapper();
    
    static final private Class W3C_DOM_NODE_CLASS, JYTHON_OBJ_CLASS;
    
    static final private ObjectWrapper JYTHON_WRAPPER;
    
    private static volatile WeakReference/*<DefaultObjectWrapper>*/ singleton2003000;
    private static volatile WeakReference/*<DefaultObjectWrapper>*/ singleton2003021;
    
    /**
     * Creates a new instance with the incompatible-improvements-version specified in
     * {@link Configuration#DEFAULT_INCOMPATIBLE_IMPROVEMENTS}.
     * 
     * @deprecated Use {@link #getInstance(Version)} or {@link #getInstance(Version, SettingAssignments)}, or
     *     in rare cases {@link #DefaultObjectWrapper(Version)} instead.
     */
    public DefaultObjectWrapper() {
        super();
    }
    
    /**
     * Use {@link #getInstance(Version)} or {@link #getInstance(Version, SettingAssignments)} instead if possible.
     * Instances created with this constructor won't share the class introspection caches with other instances.
     * 
     * @param incompatibleImprovements As of yet, the same as in {@link BeansWrapper#BeansWrapper(Version)}.
     * 
     * @since 2.3.21
     */
    public DefaultObjectWrapper(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    /**
     * Calls {@link BeansWrapper#BeansWrapper(Version, BeansWrapper.SettingAssignments)}.
     * 
     * @since 2.3.21
     */
    protected DefaultObjectWrapper(Version incompatibleImprovements, BeansWrapper.SettingAssignments settings) {
        super(incompatibleImprovements, settings);
    }
    
    /**
     * 
     * Returns an unconfigurable (read-only) {@link DefaultObjectWrapper} instance that's already configured as
     * specified in the arguments; this is preferred over using the constructors. The returned instance is often, but
     * not always a VM-wide singleton.
     * 
     * <p>The main benefit of this over the constructors is that the instances made with this method
     * share their internal class introspection caches, which is something that's expensive to build. (To be precise,
     * the introspection cache is only shared among those instances that use compatible introspection settings, like the
     * same exposure level.)
     * 
     * @param incompatibleImprovements See the corresponding parameter of
     *     {@link DefaultObjectWrapper#DefaultObjectWrapper(Version)}. Note that the version will be normalized to the
     *     lowest equivalent version, so for the returned instance {@link #getIncompatibleImprovements()} might returns
     *     a lower version than what you have specified.
     * 
     * @return A {@link DefaultObjectWrapper} (Java doesn't allow declaring that as return type here, that's only
     *      why it's declared as {@link BeansWrapper}).
     * 
     * @since 2.3.21
     */
    public static BeansWrapper getInstance(Version incompatibleImprovements) {
        return getInstance(incompatibleImprovements, SettingAssignments.DEFAULT);
    }

    /**
     * Don't call this; always fails because {@link DefaultObjectWrapper} is not affected by the
     * <tt>simpleMapWrapper</tt> setting. This method exists only so that it hides the one "inherited" from
     * {@link BeansWrapper}, which wouldn't return a {@link DefaultObjectWrapper}.
     */
    public static BeansWrapper getInstance(Version incompatibleImprovements, boolean simpleMapWrapper) {
        throw new IllegalArgumentException(
                "DefaultObjectWrapper is not affected by the simpleMapWrapper setting; "
                + "use getInstance(Version).");
    }
    
    /**
     * Same as {@link #getInstance(Version)}, but you can specify more settings of the desired instance.
     *     
     * @param settings The settings that you want to be set in the returned instance.
     * 
     * @since 2.3.21
     */
    public static BeansWrapper getInstance(Version incompatibleImprovements, SettingAssignments settings) {
        DefaultObjectWrapper res; 
        
        if (!settings.equals(SettingAssignments.DEFAULT)) {
            // We only cache an instance for the DEFAULT setting permutation.
            // This is usually not a big loss; caching the internal introspection cache is what really matters.
            return new DefaultObjectWrapper(incompatibleImprovements, settings);
        }
        
        incompatibleImprovements = normalizeIncompatibleImprovementsVersion(incompatibleImprovements);
        
        int iciInt = incompatibleImprovements.intValue();
        if (iciInt == 2003000) {
            WeakReference rw = singleton2003000;
            if (rw != null) {
                res = (DefaultObjectWrapper) rw.get();
                if (res != null) {
                    if (res._preJava5Sync != null) {
                        synchronized (res._preJava5Sync) { }  // force cache invalidation
                    }
                    return res;
                }
            }
        } else if (iciInt == 2003021) {
            WeakReference rw = singleton2003021;
            if (rw != null) {
                res = (DefaultObjectWrapper) rw.get();
                if (res != null) {
                    if (res._preJava5Sync != null) {
                        synchronized (res._preJava5Sync) { }  // force cache invalidation
                    }
                    return res;
                }
            }
        } else {
            throw new BugException();
        }
        
        res = new DefaultObjectWrapper(incompatibleImprovements, settings);
        if (res._preJava5Sync != null) {
            synchronized (res._preJava5Sync) { }  // force cache flushing
        }

        if (iciInt == 2003000) {
            singleton2003000 = new WeakReference(res);
        } else if (iciInt == 2003021) {
            singleton2003021 = new WeakReference(res);
        } else {
            throw new BugException();
        }
        
        return res;
    }
    
    static {
        Class cl;
        try {
            cl = Class.forName("org.w3c.dom.Node");
        } catch (Exception e) {
            cl = null;
        }
        W3C_DOM_NODE_CLASS = cl;
        
        ObjectWrapper ow;
        try {
            cl = Class.forName("org.python.core.PyObject");
            ow = (ObjectWrapper) Class.forName(
                    "freemarker.ext.jython.JythonWrapper")
                    .getField("INSTANCE").get(null);
        } catch (Exception e) {
            cl = null;
            ow = null;
        }
        JYTHON_OBJ_CLASS = cl;
        JYTHON_WRAPPER = ow;
    }

    public TemplateModel wrap(Object obj) throws TemplateModelException {
        if (obj == null) {
            return super.wrap(null);
        }
        if (obj instanceof TemplateModel) {
            return (TemplateModel) obj;
        }
        if (obj instanceof String) {
            return new SimpleScalar((String) obj);
        }
        if (obj instanceof Number) {
            return new SimpleNumber((Number) obj);
        }
        if (obj instanceof java.util.Date) {
            if(obj instanceof java.sql.Date) {
                return new SimpleDate((java.sql.Date) obj);
            }
            if(obj instanceof java.sql.Time) {
                return new SimpleDate((java.sql.Time) obj);
            }
            if(obj instanceof java.sql.Timestamp) {
                return new SimpleDate((java.sql.Timestamp) obj);
            }
            return new SimpleDate((java.util.Date) obj, getDefaultDateType());
        }
        if (obj.getClass().isArray()) {
            obj = convertArray(obj);
        }
        if (obj instanceof Collection) {
            return new SimpleSequence((Collection) obj, this);
        }
        if (obj instanceof Map) {
            return new SimpleHash((Map) obj, this);
        }
        if (obj instanceof Boolean) {
            return obj.equals(Boolean.TRUE) ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
        }
        if (obj instanceof Iterator) {
            return new SimpleCollection((Iterator) obj, this);
        }
        return handleUnknownType(obj);
    }
    
    
    /**
     * Called if an unknown type is passed in.
     * Since 2.3, this falls back on XML wrapper and BeansWrapper functionality.
     */
    protected TemplateModel handleUnknownType(Object obj) throws TemplateModelException {
        if ((W3C_DOM_NODE_CLASS != null && W3C_DOM_NODE_CLASS.isInstance(obj)))
        {
            return wrapDomNode(obj);
        }
        if (JYTHON_WRAPPER != null  && JYTHON_OBJ_CLASS.isInstance(obj)) {
            return JYTHON_WRAPPER.wrap(obj);
        }
        return super.wrap(obj); 
    }

    
    public TemplateModel wrapDomNode(Object obj) {
        return NodeModel.wrap((org.w3c.dom.Node) obj);
    }

    /**
     * Converts an array to a java.util.List
     */
    protected Object convertArray(Object arr) {
        final int size = Array.getLength(arr);
        ArrayList list = new ArrayList(size);
        for (int i=0;i<size; i++) {
            list.add(Array.get(arr, i));
        }
        return list;
    }
    
}
