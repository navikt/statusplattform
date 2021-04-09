import styled from 'styled-components'


const NavInfoCircleContent = styled.div`
    border: 2px solid;
    border-radius: 50%;
    min-height: 100px;
    min-width: 8.188rem;
    max-width: 14rem;
    padding: 1.5rem 0;
    text-align: center;
    > span {
        font-size: small;
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
    }
    span:nth-child(2){
        font-size: 2.5em;
    }
    @media (min-width: 560px) {
        min-width: 11.35rem;
        > span {
            line-height: 1.5rem;
            font-size: normal;
            min-height: 2.7rem;
        }
        span:nth-child(2) {
            font-size: 3.5em;
        }
    }
`;

interface InfoCircle {
    topText: string
    centerText?: string
    centerTextLeft?: string
    centerTextRight?: string
    bottomText: string
}

const NavInfoCircle = (props) => {
    const topText: string = props.topText
    const centerText: string = props.centerText
    const centerTextLeft: string = props.centerTextLeft
    const centerTextRight: string = props.centerTextRight
    const bottomText: string = props.bottomText

    if (centerText !== undefined){
        return (
            <NavInfoCircleContent>
                <span>{topText}</span>
                <span>{centerText}</span>
                <span>{bottomText}</span>
            </NavInfoCircleContent>
        )
    }
    return (
        <NavInfoCircleContent>
            <span>{topText}</span>
            <span>{centerTextLeft}/{centerTextRight}</span>
            <span>{bottomText}</span>
        </NavInfoCircleContent>
    )
}

export default NavInfoCircle