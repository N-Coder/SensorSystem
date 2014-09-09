package de.ncoder.sensorsystem;

import de.ncoder.typedmap.Key;

import java.util.Set;

public interface DependantComponent extends Component {
    public Set<Key<? extends Component>> dependencies();
}
