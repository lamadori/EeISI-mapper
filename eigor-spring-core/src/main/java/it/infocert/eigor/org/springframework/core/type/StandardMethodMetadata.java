/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.infocert.eigor.org.springframework.core.type;

import it.infocert.eigor.org.springframework.core.annotation.AnnotationAttributes;
import it.infocert.eigor.org.springframework.core.annotation.AnnotationUtils;
import it.infocert.eigor.org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * {@link MethodMetadata} implementation that uses standard reflection
 * to introspect a given {@code Method}.
 *
 * @author Juergen Hoeller
 * @author Mark Pollack
 * @author Chris Beams
 * @since 3.0
 */
public class StandardMethodMetadata implements MethodMetadata {

	private final Method introspectedMethod;

	private final boolean nestedAnnotationsAsMap;


	/**
	 * Create a new StandardMethodMetadata wrapper for the given Method.
	 * @param introspectedMethod the Method to introspect
	 */
	public StandardMethodMetadata(Method introspectedMethod) {
		this(introspectedMethod, false);
	}

	/**
	 * Create a new StandardMethodMetadata wrapper for the given Method,
	 * providing the option to return any nested annotations or annotation arrays in the
	 * form of {@link AnnotationAttributes} instead
	 * of actual {@link java.lang.annotation.Annotation} instances.
	 * @param introspectedMethod the Method to introspect
	 * @param nestedAnnotationsAsMap return nested annotations and annotation arrays as
	 * {@link AnnotationAttributes} for compatibility
	 * with ASM-based {@link AnnotationMetadata} implementations
	 * @since 3.1.1
	 */
	public StandardMethodMetadata(Method introspectedMethod, boolean nestedAnnotationsAsMap) {
		Assert.notNull(introspectedMethod, "Method must not be null");
		this.introspectedMethod = introspectedMethod;
		this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
	}

	/**
	 * Return the underlying Method.
	 */
	public final Method getIntrospectedMethod() {
		return this.introspectedMethod;
	}


	public String getMethodName() {
		return this.introspectedMethod.getName();
	}

	public String getDeclaringClassName() {
		return this.introspectedMethod.getDeclaringClass().getName();
	}

	public boolean isStatic() {
		return Modifier.isStatic(this.introspectedMethod.getModifiers());
	}

	public boolean isFinal() {
		return Modifier.isFinal(this.introspectedMethod.getModifiers());
	}

	public boolean isOverridable() {
		return (!isStatic() && !isFinal() && !Modifier.isPrivate(this.introspectedMethod.getModifiers()));
	}

	public boolean isAnnotated(String annotationType) {
		Annotation[] anns = this.introspectedMethod.getAnnotations();
		for (Annotation ann : anns) {
			if (ann.annotationType().getName().equals(annotationType)) {
				return true;
			}
			for (Annotation metaAnn : ann.annotationType().getAnnotations()) {
				if (metaAnn.annotationType().getName().equals(annotationType)) {
					return true;
				}
			}
		}
		return false;
	}

	public Map<String, Object> getAnnotationAttributes(String annotationType) {
		Annotation[] anns = this.introspectedMethod.getAnnotations();
		for (Annotation ann : anns) {
			if (ann.annotationType().getName().equals(annotationType)) {
				return AnnotationUtils.getAnnotationAttributes(
						ann, true, nestedAnnotationsAsMap);
			}
			for (Annotation metaAnn : ann.annotationType().getAnnotations()) {
				if (metaAnn.annotationType().getName().equals(annotationType)) {
					return AnnotationUtils.getAnnotationAttributes(
							metaAnn, true, this.nestedAnnotationsAsMap);
				}
			}
		}
		return null;
	}

}
