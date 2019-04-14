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

package io.github.clearwsd.verbnet.xml.util;

import io.github.clearwsd.verbnet.LogicalRelation;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * VerbNet logical relation XML adapter.
 *
 * @author jamesgung
 */
public class LogicAdapterXmlAdapter extends XmlAdapter<String, LogicalRelation> {

    @Override
    public LogicalRelation unmarshal(String value) {
        return "OR".equals(value.toUpperCase()) ? LogicalRelation.OR : LogicalRelation.AND;
    }

    @Override
    public String marshal(LogicalRelation value) {
        return value == LogicalRelation.OR ? "or" : null;
    }

}