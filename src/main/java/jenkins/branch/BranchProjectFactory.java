/*
 * The MIT License
 *
 * Copyright (c) 2011-2013, CloudBees, Inc., Stephen Connolly.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.branch;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Saveable;
import hudson.model.TopLevelItem;
import jenkins.scm.api.SCMRevision;

import java.io.File;
import java.io.IOException;

/**
 * Creates instances of the branch projects for a specific {@link Branch} and also provides some utility methods for
 * updating the branch specific projects.
 *
 * @param <P> the type of the branch projects.
 * @param <R> the type of the builds of the branch projects.
 * @author Stephen Connolly
 */
public abstract class BranchProjectFactory<P extends AbstractProject<P, R> & TopLevelItem,
        R extends AbstractBuild<P, R>>
        extends AbstractDescribableImpl<BranchProjectFactory<?, ?>> implements Saveable {

    /**
     * The owning {@link MultiBranchProject}.
     */
    @CheckForNull
    private MultiBranchProject<P, R> owner = null;

    /**
     * Creates a new branch project.
     *
     * @param branch the branch.
     * @return the new branch project instance.
     */
    public abstract P newInstance(Branch branch);

    /**
     * Saves the {@link BranchProjectFactory}
     *
     * @throws IOException if issues saving.
     */
    public void save() throws IOException {
        if (owner != null) {
            owner.save();
        }
    }

    /**
     * Sets the owner.
     *
     * @param owner the owner.
     */
    public void setOwner(MultiBranchProject<P, R> owner) {
        this.owner = owner;
    }

    /**
     * Gets the current owner.
     *
     * @return the current owner.
     */
    public MultiBranchProject<P, R> getOwner() {
        return owner;
    }

    /**
     * Gets the {@link Branch} that a specific project was configured for.
     *
     * @param project the project.
     * @return the {@link Branch} that the project was configured for.
     */
    @NonNull
    public abstract Branch getBranch(@NonNull P project);

    /**
     * Replace the {@link Branch} that a project was configured for with a new updated {@link Branch}.
     *
     * @param project the project.
     * @param branch  the new branch.
     * @return the project.
     */
    @NonNull
    public abstract P setBranch(@NonNull P project, @NonNull Branch branch);

    /**
     * Test if the specified {@link Item} is the branch project type supported by this {@link BranchProjectFactory}
     *
     * @param item the {@link Item}
     * @return {@code true} if and only if the {@link Item} is a project supported by this {@link BranchProjectFactory}.
     */
    public abstract boolean isProject(@CheckForNull Item item);

    /**
     * Casts the {@link Item} into the project type supported by this {@link BranchProjectFactory}.
     *
     * @param item the {@link Item}.
     * @return the {@link Item} upcast to the project type supported by this {@link BranchProjectFactory}.
     */
    @NonNull
    @SuppressWarnings("unchecked") // type bashing
    public P asProject(@NonNull Item item) {
        return (P) item;
    }

    /**
     * Gets the {@link SCMRevision} that the project was last built for.
     *
     * @param project the project.
     * @return the {@link SCMRevision} of the last build.
     */
    @CheckForNull
    public SCMRevision getRevision(P project) {
        XmlFile file = new XmlFile(new File(project.getRootDir(), "scm-revision-hash.xml"));
        try {
            return (SCMRevision) file.read();
        } catch (IOException e) {
            // ignore
        }
        return null;
    }

    /**
     * Sets the {@link SCMRevision} that the project was last built for.
     *
     * @param project  the project.
     * @param revision the {@link SCMRevision} of the last build.
     * @throws IOException if there was an issue persisting the details.
     */
    public void setRevisionHash(P project, SCMRevision revision) throws IOException {
        XmlFile file = new XmlFile(new File(project.getRootDir(), "scm-revision-hash.xml"));
        file.write(revision);
    }

}