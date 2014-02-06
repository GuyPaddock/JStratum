package com.redbottledesign.bitcoin.rpc.stratum.message;

/**
 * <p>{@link Result} handler for a result that's a single value.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 *
 * @param   <T>
 *          The type of data in the result.
 *
 * @author  Guy Paddock (guy.paddock@redbottledesign.com)
 */
public class ValueResult<T>
implements Result
{
    /**
     * The value of this result.
     */
    private T value;

    /**
     * Constructor for {@link ValueResult} that initializes the new
     * instance to wrap the provided value.
     *
     * @param   value
     *          The result value to wrap.
     */
    public ValueResult(T value)
    {
        this.setValue(value);
    }

    /**
     * Gets the value of this result.
     *
     * @return  The value of this result.
     */
    public T getValue()
    {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object toJson()
    {
        return this.getValue();
    }

    /**
     * Sets the value of this result.
     *
     * @param   value
     *          The new value for this result.
     */
    protected void setValue(T value)
    {
        this.value = value;
    }
}