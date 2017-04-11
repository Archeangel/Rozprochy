public enum Injury {
    KNEE("knee"), ANKLE("ankle"), ELBOW("elbow");

    private final String name;

    private Injury(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
