/**
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ngc.seaside.gradle.plugins.eclipse.updatesite.feature;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class EclipseFeatureTest {

   @Test
   public void testFromXml() throws IOException {
      Path featureXml = Paths.get("src", "test", "resources",
               EclipseFeatureTest.class.getPackage().getName().replace('.', '/'), "feature.xml");
      Reader reader = Files.newBufferedReader(featureXml);
      EclipseFeature feature = EclipseFeature.fromXml(reader);
      assertEquals("com.ngc.seaside.systemdescriptor.feature", feature.getId());
      assertEquals("JellyFish SystemDescriptor DSL", feature.getLabel());
      assertEquals("1.0.0", feature.getVersion());
      assertEquals("Northrop Grumman Corporation", feature.getProviderName());
      assertEquals("http://www.systemdescriptor.seaside.ngc.com/description", feature.getDescription().getUrl());
      assertEquals("http://www.systemdescriptor.seaside.ngc.com/copyright", feature.getCopyright().getUrl());
      assertEquals("http://www.systemdescriptor.seaside.ngc.com/license", feature.getLicense().getUrl());
      assertEquals(3, feature.getPlugins().size());
   }

   @Test
   public void testToXml() {
      EclipseFeature feature = new EclipseFeature();
      feature.setId("com.ngc.seaside.systemdescriptor.feature");
      feature.setLabel("JellyFish SystemDescriptor DSL");
      feature.setProviderName("Northrop Grumman Corporation");
      feature.setVersion("1.0.0");
      feature.plugin(p -> {
         p.setId("com.ngc.seaside.systemdescriptor");
         p.setUnpack(false);
      });
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Writer writer = new OutputStreamWriter(out);
      feature.toXml(writer);
      assertFalse(out.toString().isEmpty());
   }

}
