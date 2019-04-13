/*
 * Copyright 2019 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.clearwsd.verbnet.xml;

import io.github.clearwsd.verbnet.FrameDescription;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * XML binding implementation of {@link FrameDescription}.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = FrameDescriptionXml.ROOT_NAME)
public class FrameDescriptionXml implements FrameDescription {

    static final String ROOT_NAME = "DESCRIPTION";

    @XmlAttribute(name = "primary", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String primary;

    @XmlAttribute(name = "secondary")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String secondary = "";

    @XmlAttribute(name = "descriptionNumber", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String descriptionNumber;

    @XmlAttribute(name = "xtag", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String xtag;

}
